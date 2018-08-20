import bs4
import requests
import json
import datetime
import pyrebase

from Course import Course
from Section import Section
from JsonEncoder import ComplexEncoder
from JsonEncoder import ComplexDecoder

def main():
    start = datetime.datetime.now()
    #dump_all_courses_to_json(year=2018, season="spring")
    dump_all_courses_to_firebase(year=2018, season="fall")
    print("Start: " + str(start))
    print("End: " + str(datetime.datetime.now()))

def dump_all_courses_to_json(year, season):
    schedule_info = {}

    department_urls = get_urls_for_all_departments(year=year, season=season)
    num_deps = len(department_urls)
    counter = 1
    my_deps = ["SE", "TAM", "IE"]
    for dep_id in department_urls.keys():
        if counter >= 7:
            break
        if counter >= 3:
            dep_id = my_deps[counter-3]
        print(str(counter) + "/" + str(num_deps))
        url = department_urls[dep_id]
        schedule_info[dep_id] = get_all_department_courses(department_url=url, department_name=dep_id,
                                                           year=year, season=season)
        counter += 1

    print("printing to json.")
    with open("schedule_info.json", 'w+') as f:
        json.dump(schedule_info, f, cls=ComplexEncoder)

def dump_all_courses_to_firebase(year, season):

    # From the pyrebase github readme.
    config = None
    with open("firebase_config.json", "r") as f:
    	config = json.load(f)

    firebase = pyrebase.initialize_app(config)
    db = firebase.database()

    department_urls = get_urls_for_all_departments(year=year, season=season)
    num_deps = len(department_urls)
    counter = 1
    my_deps = ["ANTH"]
    print(type(department_urls.keys()))
    for dep_id in my_deps: #department_urls.keys()
        db.child("department_names").child(dep_id).set(dep_id)
        print(str(counter) + "/" + str(num_deps))
        url = department_urls[dep_id]
        department_courses = get_all_department_courses(department_url=url, department_name=dep_id,
                                                           year=year, season=season)

        for key in department_courses:
            course = department_courses[key]
            for gen_ed in course.gen_ed_categories:
                db.child("gen_ed_courses").child(gen_ed).child(key).set(key)
            json_course = json.dumps(course, cls=ComplexEncoder)
            db.child("departments").child(dep_id).child(key).set(json_course)
        # json_department_string = json.dumps(department_courses, cls=ComplexEncoder)
        # db.child("departments").child(dep_id).set(json_department_string)
        counter += 1

def dump_cs_courses_to_json():
    course_list = get_all_department_courses("https://courses.illinois.edu/cisapp/explorer/schedule/2018/spring/CS.xml",
                                             "CS", "2018", "spring")
    with open("cs_courses.json", 'w+') as f:
        json.dump(course_list, f, cls=ComplexEncoder)

# takes a year and a season
# returns a dictionary of all the urls where the department information is stored where each key is the department abbreviation.
def get_urls_for_all_departments(year, season):
    # e.g. https://courses.illinois.edu/cisapp/explorer/schedule/2018/spring.xml
    base_url = "https://courses.illinois.edu/cisapp/explorer/schedule/" + str(year) + "/" + season + ".xml"

    # Use requests + beautiful soup to access xml from the base page
    html_doc = requests.get(base_url).text
    if len(html_doc) < 50:
        return None
    all_deps_soup = bs4.BeautifulSoup(html_doc, "html.parser")

    departments = all_deps_soup.find_all("subject")

    department_urls = {}
    for department in departments:
        department_id = department["id"].strip() # e.g. "CS"
        department_url = department["href"].strip()
        department_urls[department_id] = department_url

    return department_urls

# Takes in the url to the xml with all the department courses, the string name of the department, the year, and the season.
# returns a dictionary with all the department's courses where each key is department_name+course_number (CS101)
def get_all_department_courses(department_url, department_name, year, season):
    # Use requests + beautiful soup to access xml from the department page which tells us all the course numbers.
    html_doc = requests.get(department_url).text
    if len(html_doc) < 50:
        return None
    dep_soup = bs4.BeautifulSoup(html_doc, "html.parser")

    # instantiate the dictionary
    course_list = {}

    # find the list of all courses in the department
    courses = dep_soup.find_all("course")
    for course in courses:
        # the course number is the value of the attribute "id"
        course_number = course["id"]
        # use the information we have to build the url where the course information is held.
        course_url = get_course_url(department=department_name, course_number=course_number, year=year, season=season)
        course_info = extract_course_info(course_url)
        course_key = department_name+course_number # e.g. "CS101"
        print(course_key)
        # add course info to the list.
        course_list[course_key] = course_info

    print("Done.")
    return course_list

# Given the department abreviation ("CS"), course number (101), year (2017), and season ("spring")
# return the url where the course's information is stored.
def get_course_url(department, course_number, year, season):
    url = "https://courses.illinois.edu/cisapp/explorer/schedule/" + str(year) + \
          "/" + season +"/" + department + "/" + str(course_number) + ".xml?mode=detail"
    # e.g "https://courses.illinois.edu/cisapp/explorer/schedule/2018/spring/MATH/241.xml?mode=detail"
    return url

# Takes in a url to a webpage containing xml for a class
# Returns a course object for the course
def extract_course_info(url):
    # using requests and beautiful soup access the xml for the course information.
    html_doc = requests.get(url).text
    if len(html_doc) < 50:
        return
    soup = bs4.BeautifulSoup(html_doc, "html.parser")

    # Create a new course and set all values except the sections..
    curr_course = parse_basic_course(soup)
    if curr_course is None:
        return None

    # Find all the soup objects for the different sections.
    sections = soup.find_all("detailedsection")
    for section in sections:
        if section is None:
            continue
        section_number = get_soup_string_value(section.find("sectionnumber"))
        if section_number is None:
            continue
        meetings = section.find_all("meeting")
        for meeting in meetings:
            curr_section = parse_section(meeting, section_number)
            curr_section_type = curr_section.type
            if curr_section_type not in curr_course.section_types:
                curr_course.section_types.append(curr_section_type)
            curr_course.section_list.append(curr_section)

    return curr_course

def parse_basic_course(soup):
    curr_course = Course()
    curr_course.name = get_soup_string_value(soup.find("label"))
    if curr_course.name is None:
        return None

    curr_course.description = get_soup_string_value(soup.find("description"))
    credit_hours_string = get_soup_string_value(soup.find("credithours"))
    curr_course.credit_hours = parse_credit_hours(credit_hours_string)

    gen_ed_categories = soup.find("genedcategories")
    if gen_ed_categories is not None:
        gen_ed_categories = gen_ed_categories.find_all("category")
        for cat in gen_ed_categories:
            curr_course.gen_ed_categories.append(cat["id"])

    return curr_course

def parse_section(soup, section_number):
    curr_section = Section()
    curr_section.section_number = section_number
    curr_section.type = get_soup_string_value(soup.find("type"))

    curr_section.days_of_the_week = get_soup_string_value(soup.find("daysoftheweek"))
    startString = get_soup_string_value(soup.find("start"))
    curr_section.start_time = parse_time(startString)
    endString = get_soup_string_value(soup.find("end"))
    curr_section.end_time = parse_time(endString)
    return curr_section


def parse_credit_hours(credit_string):
    if (credit_string is None) or ("hours" not in credit_string):
        return None
    return int(credit_string[0:1])

# Takes a string representation of a time (ex. "01:50 PM")
def parse_time(time_string):
    if (time_string is None) or (":" not in time_string):
        return None
    hours = time_string.split(":")[0] # Get the section before the ":" aka the hours
    mins = time_string.split(":")[1][:2] # Get the first two characters after the ":" aka the minutes
    hours = int(hours) % 12 # do this to make 12 = 0
    mins = int(mins)
    if "PM" in time_string:
        hours += 12 # 1PM = 13
    return (hours * 100) + mins # 10 hours and 35 minutes becomes 1035


def get_soup_string_value(soup):
    if soup is None:
        return None
    else:
        return soup.string.strip()

if __name__ == '__main__':
    main()


#Beautiful Soup info:
    # https://stackoverflow.com/questions/14257717/python-beautifulsoup-wildcard-attribute-id-search
    #sections = soup.findAll("tr", {"id" : lambda L: L and L.startswith('uid')})