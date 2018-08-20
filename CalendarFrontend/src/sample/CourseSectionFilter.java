package sample;

import sample.schedule.Section;

public interface CourseSectionFilter extends Cloneable {
    public boolean filter(Section section);
    public CourseSectionFilter copy();
//    public CourseSectionFilter clone() {
//        try {
//            return (CourseSectionFilter) super.clone();
//        } catch (CloneNotSupportedException e) {
//            e.printStackTrace();
//            throw new RuntimeException();
//        }
//    }
}
