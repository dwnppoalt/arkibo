package org.arkibo.utils;

import org.arkibo.models.ThesisModels.College;

public class CollegeNameMapper {
    public static String mapName(College college) {
        switch (college) {
            case CAG:
                return "College of Agriculture";
            case CASS:
                return "College of Arts and Social Sciences";
            case CBA: 
                return "College of Business and Accountancy";
            case CED:
                return "College of Education";
            case CEN:
                return "College of Engineering";
            case CHSI:
                return "College of Home Science and Industry";
            case COF:
                return "College of Fisheries";
            case COS:
                return "College of Sciences";
            case CVSM:
                return "College of Veterinary Science and Medicine";
            case ALL:
                return "...";
            default:
                return "...";
        }
    }
}
