import json
from Course import Course
from Section import Section

class ComplexEncoder(json.JSONEncoder):
    def default(self, obj):
        if hasattr(obj,'reprJSON'):
            return obj.reprJSON()
        else:
            return json.JSONEncoder.default(self, obj)

class ComplexDecoder(json.JSONDecoder):
    def default(self, d):
        if d is None:
            return None
        if 'name' in d and 'section_list' in d:
            object = Course(d)
            section_list_dict = d["section_list"]
            object.section_list = list()
            for section_dict in section_list_dict:
                object.section_list.append(self.default(section_dict))
            return object
        elif 'section_number' in d and 'type' in d:
            object = Section(d)
            return object
        else:
            return d