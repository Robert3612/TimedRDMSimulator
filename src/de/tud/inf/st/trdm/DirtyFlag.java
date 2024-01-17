package de.tud.inf.st.trdm;

import java.util.List;

public class DirtyFlag {
    List<Integer> dirtyFlag;

    public DirtyFlag(List<Integer> data){
        this.dirtyFlag = data;
    }

    public List<Integer> getDirtyFlag(){
        return dirtyFlag;
    }

    public void setDirtyFlag(List<Integer> dirtyFlag){
        this.dirtyFlag = dirtyFlag;
    }

    // 0: kleiner, 1: größer, 2: gleich
    public int compareFlag(List<Integer> newest){
        for(int i=0;i<dirtyFlag.size();i++){
            if(dirtyFlag.get(i) < newest.get(i)){
                return 0;
            }
            if(dirtyFlag.get(i) > newest.get(i)){
                return 1;
            }
        }
        return 2;
    }

    public boolean equalDirtyFlag(List<Integer> otherDirtyFlag){
        if(dirtyFlag.size() != otherDirtyFlag.size()){
            return false;
        }
        for(int i=0;i<dirtyFlag.size();i++){
            if(dirtyFlag.get(i) != otherDirtyFlag.get(i)){
                return false;
            }
        }
        return true;
    }


    public String toString(){
        StringBuilder answer = new StringBuilder();
        for(Integer i:dirtyFlag){
            answer.append(i).append(".");
        }
        if(!answer.isEmpty()) {
            answer.deleteCharAt(answer.length()-1);
        }
        return answer.toString();
    }
}
