package org.summarizer;

public class Information {
	public String word;
    public int frequency;
    
    Information(String word, int frequency){
        this.word = word;
        this.frequency = frequency;
    }
    
    @Override
    public String toString(){
        return(word + ", " + frequency);
    }
}
