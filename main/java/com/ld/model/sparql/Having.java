package com.ld.model.sparql;

public class Having implements Cloneable{

	public String filter;

	@Override public Having clone()
	{
		return new Having(filter);
	}

	public Having(String s) {
		filter = s;
	}

	public String toString() {
		return "HAVING (" + filter + ")";
	}
}
