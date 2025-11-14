package rubikscube;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.PriorityQueue; // For A* (sorts by f-score)
import java.util.Map;           // To track visited states
import java.util.HashMap;

public class Solver {


	private static class SearchNode implements Comparable<SearchNode>{
		public RubiksCube state;
		String path;// moves taken to reach this state
		public int moves; // how many moves so far 
		public int h;// heuristic estimate to goal
		public int f; // f = g + h  total estimated cost

		public SearchNode (RubiksCube state, String path) {
			this.state = state;
			this.path = path;
			this.moves = path.length();
			this.h = state.calculateHeuristic();
			this.f = this.moves + this.h;
		}	

		@Override
		public int compareTo(SearchNode other) {
			return Integer.compare(this.f, other.f);
	}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if(obj == null|| this.getClass() != obj.getClass()){
				return false;
			}
			SearchNode other = (SearchNode) obj;
			return this.state.toString().equals(other.state.toString());
		
		}
		@Override
		public int hashCode() {
			return this.state.toString().hashCode();
		}
public static String solve(RubiksCube ScrambbledCube){
	
}

	public static void main(String[] args) {
//		System.out.println("number of arguments: " + args.length);
//		for (int i = 0; i < args.length; i++) {
//			System.out.println(args[i]);
//		}

		if (args.length < 2) {
			System.out.println("File names are not specified");
			System.out.println("usage: java " + MethodHandles.lookup().lookupClass().getName() + " input_file output_file");
			return;
		}
		
		
		// TODO
		//File input = new File(args[0]);
		// solve...
		//File output = new File(args[1]);

	}
}
