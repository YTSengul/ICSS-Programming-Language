package nl.han.ica.icss.generator;

import nl.han.ica.icss.ast.*;

public class Generator {

    private final String LINESEPERATOR = System.lineSeparator();

    // Zet de generator klaar
	public String generate(AST ast) {
        StringBuilder string = new StringBuilder();
        for (ASTNode node : ast.root.getChildren()) {
            string.append(generateOutpuCSS(node));

        }
        return string.toString();
    }

    // Implementatie om de generator over te zetten CSS2-Compliant string
    public String generateOutpuCSS(ASTNode node) {
        StringBuilder outputCSS = new StringBuilder();

        if (node instanceof Stylerule) {
            for (Selector selector : ((Stylerule) node).selectors) {
                outputCSS.append(selector.toString()).append(" ");
            }
            outputCSS.append('{').append(LINESEPERATOR);


            for (ASTNode declaration : node.getChildren()) {
                if (declaration instanceof Declaration) {

                    // Deze twee regels hieronder zijn voor GE02.
                    // Dit houdt in dat er twee spaties insgesprongen wordt per scopeniveau.
					String SPACINGTAB = "\t";
					outputCSS.append(SPACINGTAB);

                    outputCSS.append(((Declaration) declaration).property.name).append(": ");
                    outputCSS.append(((Declaration) declaration).expression.toString());
                    outputCSS.append(';');
                    outputCSS.append(LINESEPERATOR);
                }
            }
            outputCSS.append('}').append(LINESEPERATOR).append(LINESEPERATOR);
        }

        return outputCSS.toString();
    }
}
