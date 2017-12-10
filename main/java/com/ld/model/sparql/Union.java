/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ld.model.sparql;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author christina
 */
public class Union {

    Set<Triple> conditions_left;
    Set<Filter> filters_left;
    Set<Triple> conditions_right;
    Set<Filter> filters_right;

    public Union() {
        conditions_left = new HashSet<Triple>();
        filters_left = new HashSet<Filter>();
        conditions_right = new HashSet<Triple>();
        filters_right = new HashSet<Filter>();
    }
    public Union(Set<Triple> c_left,Set<Filter> f_left,Set<Triple> c_right,Set<Filter> f_right) {
        conditions_left = c_left;
        filters_left = f_left;
        conditions_right = c_right;
        filters_right = f_right;
    }

    public String toString() {
        String out = "{ ";
        for (Triple t : conditions_left) out += t.toString();
        for (Filter f : filters_left) out += f.toString();
        out += " } UNION {";
        for (Triple t : conditions_right) out += t.toString();
        for (Filter f : filters_right) out += f.toString();
        out += "}";
        return out;
    }

}
