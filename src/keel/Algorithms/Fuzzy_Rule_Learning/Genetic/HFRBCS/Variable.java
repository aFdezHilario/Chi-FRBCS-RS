/*
 * Copyright (C) 2014 Mikel Elkano Ilintxeta
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package keel.Algorithms.Fuzzy_Rule_Learning.Genetic.HFRBCS;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;

/**
 * Represents a variable of the problem
 * @author Mikel Elkano Ilintxeta
 * @version 1.0
 */
public abstract class Variable implements Serializable {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = -7838631614820856969L;
	/**
     * Variable name
     */
    private String name;
    
    /**
     * Creates a new variable
     * @param name variable name
     */
    protected Variable (String name){
    	
    	this.name = name;
    	
    }
    
    /**
     * Returns the variable label index corresponding to the input value
     * @param inputValue input value
     * @return Variable label index corresponding to the input value
     */
    public abstract byte getLabelIndex(String inputValue);
    
    /**
     * 
     */
    public abstract Variable clone();
    
    //public abstract void write(DataOutput out) throws IOException;
    
    //public abstract void readFields(DataInput in) throws IOException;
    
    /**
     * Returns the variable name
     * @return variable name
     */
    public String getName (){
        return name;
    }

}
