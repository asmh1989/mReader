// HTMLParser Library - A java-based parser for HTML
// http://htmlparser.org
// Copyright (C) 2006 Somik Raha
//
// Revision Control Information
//
// $URL: http://svn.code.sf.net/p/htmlparser/code/trunk/parser/src/main/java/org/htmlparser/tags/DefinitionListBullet.java $
// $Author: derrickoswald $
// $Date: 2011-04-25 17:39:12 +0800 (周一, 2011-04-25) $
// $Revision: 74 $
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the Common Public License; either
// version 1.0 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// Common Public License for more details.
//
// You should have received a copy of the Common Public License
// along with this library; if not, the license is available from
// the Open Source Initiative (OSI) website:
//   http://opensource.org/licenses/cpl1.0.php

package org.htmlparser.tags;

/**
 * A definition list bullet tag (either DD or DT).
 */
public class DefinitionListBullet extends CompositeTag
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
     * The set of names handled by this tag.
     */
    private static final String[] mIds = new String[] {"DD", "DT"};

    /**
     * The set of tag names that indicate the end of this tag.
     */
    private static final String[] mTagEnders = new String[] {"DD", "DT", "DL"};

    /**
     * The set of end tag names that indicate the end of this tag.
     */
    private static final String[] mEndTagEnders = new String[] {"DL", "BODY", "HTML"};

    /**
     * Create a new bullet tag.
     */
    public DefinitionListBullet ()
    {
    }

    /**
     * Return the set of names handled by this tag.
     * @return The names to be matched that create tags of this type.
     */
    public String[] getIds ()
    {
        return (mIds);
    }

    /**
     * Return the set of tag names that cause this tag to finish.
     * @return The names of following tags that stop further scanning.
     */
    public String[] getEnders ()
    {
        return (mTagEnders);
    }

    /**
     * Return the set of end tag names that cause this tag to finish.
     * @return The names of following end tags that stop further scanning.
     */
    public String[] getEndTagEnders ()
    {
        return (mEndTagEnders);
    }
}
