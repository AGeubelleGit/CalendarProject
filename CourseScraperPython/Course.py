class Course:
    name = None
    description = None
    credit_hours = None
    gen_ed_categories = list()
    section_list = list()
    section_types = list()

    def __init__(self, json_dict=None):
        if json_dict is None:
            self.name = None
            self.description = None
            self.credit_hours = None
            self.gen_ed_categories = list()
            self.section_list = list()
            self.section_types = list()
        else:
            self.name = json_dict['name']
            self.description = json_dict['description']
            self.credit_hours = json_dict['credit_hours']
            self.gen_ed_categories = json_dict['gen_ed_categories']
            self.section_types = json_dict['section_types']

    def course_to_string(self):
        output = self.name + "\n"
        output += self.description + "\n"
        output += "credit hours: " + str(self.credit_hours) + "\n"
        output += "Gen ed categories: "
        if len(self.gen_ed_categories) <= 0:
            output += "None\n"
        for category in self.gen_ed_categories:
            output += category + " "
        output += "\n"

        for section_type in self.section_types:
            output += section_type + " "
        output += "\n"
        for section in self.section_list:
            output += section.section_to_string() + "\n"
        return output

    def reprJSON(self):
        return dict(name = self.name, description=self.description, credit_hours=self.credit_hours,
                    gen_ed_categories=self.gen_ed_categories, section_list=self.section_list, section_types=self.section_types)

