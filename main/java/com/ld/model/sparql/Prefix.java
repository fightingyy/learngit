package com.ld.model.sparql;

import java.io.Serializable;

public class Prefix implements Serializable, Cloneable {

	private static final long serialVersionUID = 1971250768286898228L;

	private String name;
	private String url;

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}

	@Override public Prefix clone()
	{
		return new Prefix(name, url);
	}

	@Override
	public String toString() {
		return "PREFIX "+name+": <"+url+">";
	}

	public Prefix(String name, String url) {
		super();
		this.name = name;
		this.url = url;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Prefix other = (Prefix) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		return true;
	}


}
