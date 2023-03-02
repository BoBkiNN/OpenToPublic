package xyz.bobkinn_.opentopublic;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class ThreeLean<A, B, C>{
    private int currentI = 0;
    private Object current;
    private final ArrayList<Object> list;

    /**
     * Create new ThreeLean with custom types
     * @param a state one
     * @param b state two
     * @param c state three
     */
    public ThreeLean(A a, B b, C c){
        list = new ArrayList<>();
        list.add(a);
        list.add(b);
        list.add(c);
        current = a;
    }

    /**
     * Get list of values
     * @return values list
     */
    public ArrayList<Object> getList(){
        return list;
    }

    /**
     * Set current to next value
     */
    public void next(){
        Object o = list.get(currentI);
        if (currentI >= list.size()-1) currentI = 0;
        else currentI += 1;
        current = o;
    }

    /**
     * Get current state as Object
     * @return current state as Object
     */
    public Object getCurrentRaw(){
        return current;
    }

    /**
     * Get current type
     * @return index of current type value, in order of constructor arguments
     */
    public int currentType(){
        return currentI;
    }

    /**
     * Get A object
     * @return A or null if current is other
     */
    public A getA(){
        if (current != null) return null;
        try {
            return (A) current;
        } catch (ClassCastException ignored){
            return null;
        }
    }

    /**
     * Get B object
     * @return B or null if current is other
     */
    public B getB(){
        if (current != null) return null;
        try {
            return (B) current;
        } catch (ClassCastException ignored){
            return null;
        }
    }

    /**
     * Get C object
     * @return C or null if current is other
     */
    public C getC(){
        if (current != null) return null;
        try {
            return (C) current;
        } catch (ClassCastException ignored){
            return null;
        }
    }

    /**
     * Get new ThreeLean with states: true false and passed String
     * @param third third state
     * @return ThreeLean that have third state as String
     */
    @Contract("_ -> new")
    public static @NotNull ThreeLean<Boolean, Boolean, String> newBBS(String third){
        return new ThreeLean<>(true, false, third);
    }

}
