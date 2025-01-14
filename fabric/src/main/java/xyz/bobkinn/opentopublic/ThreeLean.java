package xyz.bobkinn.opentopublic;

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
        list = new ArrayList<>(3);
        list.add(a);
        list.add(b);
        list.add(c);
        current = a;
    }

    @Override
    public String toString() {
        return current.toString();
    }

    /**
     * Set current state using it index
     * @param index state index, in order as passed type parameters
     */
    public void setCurrentState(int index){
        if (index < list.size() && index > -1) {
            current = list.get(index);
            currentI = index;
        }
    }

    /**
     * Get list of values
     * @return values list
     */
    @SuppressWarnings("unused")
    public ArrayList<Object> getList(){
        return list;
    }

    /**
     * Set current to next value
     */
    public void next(){
        if (currentI >= list.size()-1) currentI = 0;
        else currentI = currentI + 1;
        current = list.get(currentI);
    }

    /**
     * Get current state as Object
     * @return current state as Object
     */
    @SuppressWarnings("unused")
    public Object getCurrentRaw(){
        return current;
    }

    /**
     * Get current type index
     * @return index of current type value, in order of constructor arguments
     */
    @SuppressWarnings("unused")
    public int currentType(){
        return currentI;
    }

    /**
     * Get A object
     * @return A
     */
    @SuppressWarnings({"unused", "unchecked"})
    public A getA(){
        return (A) list.getFirst();
    }

    /**
     * Get B object
     * @return B
     */
    @SuppressWarnings({"unused", "unchecked"})
    public B getB(){
        return (B) list.get(1);
    }

    /**
     * Get C object
     * @return C
     */
    @SuppressWarnings({"unused", "unchecked"})
    public C getC(){
        return (C) list.get(2);
    }

    public boolean isThird(){
        return currentI == 2;
    }

    /**
     * Checks is current state is boolean {@code true}
     * @return {@code true} or {@code false} if current state is {@code false} or other
     */
    @SuppressWarnings("all")
    public boolean isTrue(){
        try {
            boolean val = (Boolean) current;
            return val == true;
        } catch (ClassCastException ignored){
            return false;
        }
    }

    /**
     * Checks is current state is boolean {@code false}
     * @return {@code true} or {@code false} if current state is {@code true} or other
     */
    @SuppressWarnings("all")
    public boolean isFalse(){
        try {
            boolean val = (Boolean) current;
            return val == false;
        } catch (ClassCastException ignored){
            return false;
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
