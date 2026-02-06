package org.arkibo;

import org.arkibo.models.ThesisModels.Thesis;
import org.arkibo.repository.ThesisRepository;

public class Test {
    public static void main(String[] args) {
        ThesisRepository tr = new ThesisRepository();
        Thesis thesis = tr.thesisInfo(4).data();

        System.out.println(thesis.title());
        System.out.println(thesis.abstractText());
    }
}
