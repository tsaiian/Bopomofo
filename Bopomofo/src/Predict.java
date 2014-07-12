import java.io.File;
import java.io.FileReader;

import opennlp.maxent.BasicContextGenerator;
import opennlp.maxent.ContextGenerator;
import opennlp.maxent.DataStream;
import opennlp.maxent.PlainTextByLineDataStream;
import opennlp.model.GenericModelReader;
import opennlp.model.MaxentModel;

public class Predict {
	MaxentModel _model;
	ContextGenerator _cg = new BasicContextGenerator();

	public Predict(MaxentModel m) {
		_model = m;
	}

	private void eval(String predicates) {
		String[] contexts = predicates.split(" ");
		double[] ocs = _model.eval(contexts);

		boolean print = false;
		for (double d : ocs)
			if (d > 0.001) {
				print = true;
				System.out.print(predicates.split(" ")[0].substring(5) + "(" + _model.getBestOutcome(ocs) + ") ");
				break;
			}
		if (!print)
			System.out.print(predicates.split(" ")[0].substring(5) + " ");
	}

	/**
	 * Main method. Call as follows:
	 * <p>
	 * java Predict dataFile (modelFile)
	 */

	public static void main(String[] args) {
		String dataFileName = null, modelFileName = "outModel.txt";

		if (args.length == 0) {
			System.out.println("java Predict dataFile (modelFile)");
			System.exit(0);
		}
		if (args.length >= 1)
			dataFileName = args[0];
		if (args.length >= 2)
			modelFileName = args[1];

		Predict predictor = null;
		try {
			MaxentModel m = new GenericModelReader(new File(modelFileName)).getModel();
			predictor = new Predict(m);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		try {
			DataStream ds = new PlainTextByLineDataStream(new FileReader(new File(dataFileName)));

			while (ds.hasNext()) {
				String s = (String) ds.nextToken();
				for (int i = 0; i < s.length(); i++) {
					String sn1 = (i - 1 >= 0 ? s.charAt(i - 1) : 'X') + "";
					String s0 = s.charAt(i) + "";
					String s1 = (i + 1 <= s.length() - 1 ? s.charAt(i + 1) : 'X') + "";

					predictor.eval("word=" + s0 + " pre=" + sn1 + s0 + " suf=" + s0 + s1);
				}
				System.out.println();
			}
			return;
		} catch (Exception e) {
			System.out.println("Unable to read from specified file: " + modelFileName);
			System.out.println();
			e.printStackTrace();
		}
	}
}
