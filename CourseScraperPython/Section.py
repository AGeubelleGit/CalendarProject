class Section:
    section_number = None
    type = None
    days_of_the_week = None
    start_time = None
    end_time = None

    def __init__(self, json_dict=None):
        if json_dict is None:
            self.section_number = None
            self.type = None
            self.days_of_the_week = None
            self.start_time = None
            self.end_time = None
        else:
            self.section_number = json_dict['section_number']
            self.type = json_dict['type']
            self.days_of_the_week = json_dict['days_of_the_week']
            self.start_time = json_dict['start_time']
            self.end_time = json_dict['end_time']

    def section_to_string(self):
        output = ""
        output += "SectionNumber: " + self.var_to_string(self.section_number) + "\n"
        output += "Type: " + self.var_to_string(self.type) + "\n"
        output += "Days of the week: " + self.var_to_string(self.days_of_the_week) + "\n"
        output += "Start/End: " + self.var_to_string(self.start_time) \
                  + " to " + self.var_to_string(self.end_time) + "\n"
        return output

    def var_to_string(self, variable):
        if variable is None:
            return "NONE"
        else:
            return str(variable)

    def reprJSON(self):
        return dict(section_number = self.section_number, type=self.type, days_of_the_week = self.days_of_the_week,
                    start_time = self.start_time, end_time = self.end_time)