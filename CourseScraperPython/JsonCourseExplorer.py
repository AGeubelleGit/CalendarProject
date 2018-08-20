import json

from Course import Course
from Section import Section
from JsonEncoder import ComplexDecoder

def main():
    # print_cs_courses_to_json()
    course_list = decode_courses("schedule_info.json")
    print(course_list.keys())
    print(course_list["REL"]["REL110"].course_to_string())


def decode_courses(file_name):
    json_string = None
    with open(file_name, 'r') as f:
        json_string = json.load(f)

    if json_string is None:
        return None

    course_list = {}
    for department_id in json_string.keys():
        department_dict = json_string[department_id]
        department_coures = {}
        for course_key in department_dict.keys():
            course_json = department_dict[course_key]
            decoder = ComplexDecoder()
            course_info = decoder.default(course_json)
            department_coures[course_key] = course_info
        course_list[department_id] = department_coures
    return course_list

if __name__ == '__main__':
    main()
