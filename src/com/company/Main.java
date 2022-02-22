package com.company;
import java.util.*;

public class Main {

    public static void main(String[] args) {
        Scanner in= new Scanner(System.in);
        fuzzy obj = new fuzzy();
        Vector<subSet> sub= new Vector();

        //add variable 1, input
        sub.add(new subSet("veryLow",4, new float[]{0,0,10,30}));
        sub.add(new subSet("low",4, new float[]{10,30,40,60}));
        sub.add(new subSet("medium",4, new float[]{40,60,70,90}));
        sub.add(new subSet("high",4, new float[]{70,90,100,100}));
        obj.addFuzzySet("project_funding", sub, "input");

        //add variable 2, input
        sub= new Vector<subSet>();
        sub.add(new subSet("beginner",3, new float[]{0,15,30}));
        sub.add(new subSet("intermediate",3, new float[]{15,30,45}));
        sub.add(new subSet("expert",3, new float[]{30,60,60}));
        obj.addFuzzySet("team_experience_level", sub, "input");

        //add variable 3, output
        sub= new Vector<subSet>();
        sub.add(new subSet("high",3, new float[]{0,25,50}));
        sub.add(new subSet("normal",3, new float[]{25,50,75}));
        sub.add(new subSet("low",3, new float[]{50,100,100}));
        obj.addFuzzySet("risk", sub, "output");

        //print(obj);

        //add rules
        obj.addRules("project_funding=high OR team_experience_level=expert -> risk=low");
        obj.addRules("project_funding=medium AND team_experience_level=intermediate OR team_experience_level=beginner -> risk=normal");
        obj.addRules("project_funding=veryLow -> risk=high");
        obj.addRules("project_funding=low AND team_experience_level=beginner -> risk=high");

        //start
        Vector<String> name= new Vector(); //name.add("project_funding"); name.add("team_experience_level");
        Vector<Float> crisp= new Vector(); //crisp.add((float) 50); crisp.add((float) 40);

        System.out.print("Enter Number of input variables: "); int size= in.nextInt();
        System.out.print("Enter Variable Name and its crisp input:");
        for(int i=0; i<size; i++){
            System.out.print("\nVariable-"+(i+1)+ " Name: "); name.add(in.next());
            System.out.print("Variable-"+(i+1)+ " Crisp Value: "); crisp.add(in.nextFloat());

        }

        obj.Run(name,crisp);

    }

    public static void print(fuzzy obj){
        for (Map.Entry<String, Vector<subSet> > idx : obj.fSets.entrySet()) {
            System.out.println("\n"+ idx.getKey());
            for (int i=0; i< idx.getValue().size(); i++){
                System.out.print(idx.getValue().get(i).name+ ": [");

                for(int j=0; j<idx.getValue().get(i).type; j++) {
                    System.out.print(idx.getValue().get(i).values[j]);
                    if(j != idx.getValue().get(i).type-1)
                        System.out.print(" ,");
                }
                System.out.println("]");
            }
        }
    }
}
