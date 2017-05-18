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
 * Represents a fuzzy set
 * @author Mikel Elkano Ilintxeta
 * @version 1.0
 */
public class FuzzySet implements Serializable{
	
	/*
	 * If you are analyzing a high dimensional dataset consider using float type variables
	 * instead of doubles in order to save memory.
	 */
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 5573725809361567320L;

	/**
     * Left point of the triangle
     */
    private double leftPoint;
    
    /**
     * Mid point of the triangle
     */
    private double midPoint;
    
    /**
     * Right point of the triangle
     */
    private double rightPoint;
    
    /**
     * Linguistic label associated with this fuzzy set
     */
    private byte labelIndex;
    
    /**
     * Creates a fuzzy set modeled by a triangular membership function
     * @param leftPoint left point of the triangle
     * @param midPoint mid point of the triangle
     * @param rightPoint right point of the triangle
     * @param labelIndex index of the linguistic label associated with this fuzzy set
     */
    public FuzzySet (double leftPoint, double midPoint, double rightPoint, byte labelIndex){
        
        this.leftPoint = leftPoint;
        this.midPoint = midPoint;
        this.rightPoint = rightPoint;
        this.labelIndex = labelIndex;
        
    }
    
    /**
     * Copy constructor
     * @param fs another Fuzzy Set
     */
    public FuzzySet (FuzzySet fs){
        
        this.leftPoint = fs.leftPoint;
        this.midPoint = fs.midPoint;
        this.rightPoint = fs.rightPoint;
        this.labelIndex = fs.labelIndex;
        
    }
    
    public FuzzySet(){
    	
    }
    
    /**
     * Returns the linguistic label associated with this fuzzy set
     * @return linguistic label associated with this fuzzy set
     */
    public byte getLabelIndex (){
        return labelIndex;
    }
    
    /**
     * Returns the left point of the triangle
     * @return left point of the triangle
     */
    public double getLeftPoint (){
        return leftPoint;
    }
    
    /**
     * XX
     * 
     */
    public void setLeftPoint (double value){
        leftPoint = value;
    }
    
    /**
     * Returns the membership degree of the input value to this fuzzy set
     * @param value input value
     * @return membership degree of the input value to this fuzzy set
     */
    public double getMembershipDegree (double value){
        
        if (leftPoint <= value && value <= midPoint){
            if (leftPoint == value && midPoint == value)
                return 1.0;
            else
                return (value - leftPoint) / (midPoint - leftPoint);
        }
        else if (midPoint <= value && value <= rightPoint){
            if (midPoint == value && rightPoint == value)
                return 1.0;
            else
                return (rightPoint - value) / (rightPoint - midPoint);
        }
        else
            return 0;
        
    }
    
    /**
     * Returns the mid point of the triangle
     * @return mid point of the triangle
     */
    public double getMidPoint (){
        return midPoint;
    }
    
    /**
     * XX
     * 
     */
    public void setMidPoint (double value){
        midPoint = value;
    }
    
    /**
     * Returns the right point of the triangle
     * @return right point of the triangle
     */
    public double getRightPoint (){
        return rightPoint;
    }
    
    /**
     * XX
     * 
     */
    public void setRightPoint (double value){
        rightPoint = value;
    }
    
    /*
    @Override
    public void write(DataOutput out) throws IOException{
		out.writeDouble(leftPoint);
		out.writeDouble(midPoint);
		out.writeDouble(rightPoint);
		out.writeByte(labelIndex);					
    }

	@Override
	public void readFields(DataInput in) throws IOException {
		leftPoint = in.readDouble();
		midPoint = in.readDouble();
		rightPoint  = in.readDouble();
		labelIndex = in.readByte();	
	}
	*/

}
