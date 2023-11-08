package edu.asu.effortlogger.logs.model;

import java.util.ArrayList;
import java.util.List;

public record Project(String name, List<Integer> lcStepIdx, List<LifeCycle> lcSteps) {

    public static Project createFrom(String name, String lcStepStr, ArrayList<LifeCycle> lc) {
        String[] lcSep = lcStepStr.split(",");
        var lsi = new ArrayList<Integer>();
        var ls = new ArrayList<LifeCycle>();
        for (String num : lcSep){
            int i = Integer.parseInt(num) - 1;
            lsi.add(i);
            ls.add(lc.get(i));
        }

        return new Project(name, lsi, ls);
    }

    @Override
    public String toString() {
        return name;
    }
}
