package nl.han.ica.icss.parser;

import java.util.Stack;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.icss.ast.selectors.ClassSelector;
import nl.han.ica.icss.ast.selectors.IdSelector;
import nl.han.ica.icss.ast.selectors.TagSelector;

/**
 * This class extracts the ICSS Abstract Syntax Tree from the Antlr Parse tree.
 */
public class ASTListener extends ICSSBaseListener {

	//Accumulator attributes:
	private AST ast;

	//Use this to keep track of the parent nodes when recursively traversing the ast
	private Stack<ASTNode> currentContainer;

	public ASTListener() {
		ast = new AST();
		currentContainer = new Stack<>();
	}

    public AST getAST() {
        return ast;
    }

    //Zet de root object in de array currentContainer.
	@Override
	public void enterStylesheet(ICSSParser.StylesheetContext ctx) {
		currentContainer.push(ast.root);
	}

	//Haal de object uit de currentcontainer
	@Override
	public void exitStylesheet(ICSSParser.StylesheetContext ctx) {
		currentContainer.pop();
	}

	// Stopt de stylerules in de currentContainer
	// Deze zou bij level0 er 4 moeten zijn.
	@Override
	public void enterStylerule(ICSSParser.StyleruleContext ctx) {
		ASTNode styleRule = new Stylerule();
		currentContainer.peek().addChild(styleRule);
		currentContainer.push(styleRule);
	}

	@Override
	public void exitStylerule(ICSSParser.StyleruleContext ctx) {
		currentContainer.pop();
	}

	// De aantal contenten in een stylerule. bv p {color: #000000; color:
	// #ff9898; } zijn er 2. als hier 1x color in zou zitten zouden er hier
	// maar 1 van in te zien zijn in de stylerule.
	@Override
	public void enterDeclaration(ICSSParser.DeclarationContext ctx) {
		ASTNode declaration = new Declaration();
		currentContainer.peek().addChild(declaration);
		currentContainer.push(declaration);
	}

	@Override
	public void exitDeclaration(ICSSParser.DeclarationContext ctx) {
		currentContainer.pop();
	}

	// Dit geeft voor ieder declaration de property (Naam van wat het is.
	// background-color, width?
	@Override
	public void enterProperty(ICSSParser.PropertyContext ctx) {
		currentContainer.peek().addChild(new PropertyName(ctx.getText()));
	}

	// Id van een selector, bij level0 is dit de #menu
	@Override
	public void enterSelectorId(ICSSParser.SelectorIdContext ctx) {
		currentContainer.peek().addChild(new IdSelector(ctx.getText()));
	}

	// Deze laat de classes zien. deze beginnen met een . (punt) bij css.
	// In level0 is dit de .menu .
	@Override
	public void enterSelectorClass(ICSSParser.SelectorClassContext ctx) {
		currentContainer.peek().addChild(new ClassSelector(ctx.getText()));
	}

	//Deze laat de tag in de CSS zien, dit is dan in de level 0 de p. (p van paragraph)
	@Override
	public void enterSelectorTag(ICSSParser.SelectorTagContext ctx) {
		currentContainer.peek().addChild(new TagSelector(ctx.getText()));
	}

	// Deze laat de kleurcode zien van een property. bij level 0 tagselector p
	// is dit de kleur #ffffff van de property background-color. (Is het trouwens geen colour in het engels???)
	@Override
	public void enterColor(ICSSParser.ColorContext ctx) {
		currentContainer.peek().addChild(new ColorLiteral(ctx.getText()));
	}

	// Deze laat de width in pixelsize zien in de internal AST. Dit is bij level
	// 0 de waarde 500 van pixel literal van property width.
	@Override
	public void enterPixelsize(ICSSParser.PixelsizeContext ctx) {
		currentContainer.peek().addChild(new PixelLiteral(ctx.getText()));
	}

	// Deze laat de waarde in percentage in de internal AST zien. geen voorbeeld
	// uit de testdocumenten volgens mij.
	@Override
	public void enterPercentage(ICSSParser.PercentageContext ctx) {
		currentContainer.peek().addChild(new PercentageLiteral(ctx.getText()));
	}

	// Dit laat de waarde van een parameter zien. bv bij level2 is dit de color
	// literal van (#ff0000).
	@Override
	public void enterVariableReference(ICSSParser.VariableReferenceContext ctx) {
		currentContainer.peek().addChild(new VariableReference(ctx.getText()));
	}

	// Deze methode zorg ervoor dat de gemaakte variabelen in stukken worden gedeeld.
	// Zeg maar in een nieuwe tak apart in de boom.
	@Override
	public void enterVariableAssigning(ICSSParser.VariableAssigningContext ctx) {
		ASTNode assignment = new VariableAssignment();
		currentContainer.peek().addChild(assignment);
		currentContainer.push(assignment);
	}

	@Override
	public void exitVariableAssigning(ICSSParser.VariableAssigningContext ctx) {
		currentContainer.pop();
	}

	// Dit zorgt ervoor dat je in de bool de 'TRUE' of 'FALSE' kunt inzien. (level 1 is voorbeeld)
	// De Parser geeft aan of het true is of niet. zo niet, dan wordt er false in de
	// currentcontainer gestopt. dit gebeurt door middel van een if expressie hier in de code.
	@Override
	public void enterBool(ICSSParser.BoolContext ctx) {
		Expression exp;
		if (ctx.FALSE() != null) {
			exp = new BoolLiteral(false);
		} else {
			exp = new BoolLiteral(true);
		}
		currentContainer.peek().addChild(exp);
	}

	// Deze laat de if expressie in de AST zien. Of deze er is of niet. level 3 is voorbeeld ervan.
	@Override
	public void enterIf_statement(ICSSParser.If_statementContext ctx) {
		ASTNode ifStatement = new IfClause();
		currentContainer.peek().addChild(ifStatement);
		currentContainer.push(ifStatement);
	}

	@Override
	public void exitIf_statement(ICSSParser.If_statementContext ctx) {
		currentContainer.pop();
	}

	// ----------------------------------------------------------------------------
	@Override
	public void enterScalar(ICSSParser.ScalarContext ctx) {
		currentContainer.peek().addChild(new ScalarLiteral(ctx.getText()));
	}

	// XZorg ervoor dat getallen bij elkaar opgeteld kunnen worden.
	@Override
	public void enterAddOperation(ICSSParser.AddOperationContext ctx) {
		Operation operation = new AddOperation();
		currentContainer.peek().addChild(operation);
		currentContainer.push(operation);
	}

	@Override
	public void exitAddOperation(ICSSParser.AddOperationContext ctx) {
		Operation o = (Operation)currentContainer.pop();
	}

	// Zorgt ervoor dat waarden vermenigvuldigd kunnen worden.
	@Override
	public void enterMultiplyOperation(ICSSParser.MultiplyOperationContext ctx) {
		Operation operation = new MultiplyOperation();
		currentContainer.peek().addChild(operation);
		currentContainer.push(operation);
	}

	@Override
	public void exitMultiplyOperation(ICSSParser.MultiplyOperationContext ctx) {
		Operation o = (Operation)currentContainer.pop();
	}

	// Zorgt ervoor dat er waarden van elkaar af getrokken kunnen worden.
	@Override
	public void enterSubstractOperation(ICSSParser.SubstractOperationContext ctx) {
		Operation operation = new SubtractOperation();
		currentContainer.peek().addChild(operation);
		currentContainer.push(operation);
	}
	@Override
	public void exitSubstractOperation(ICSSParser.SubstractOperationContext ctx) {
		Operation o = (Operation)currentContainer.pop();
	}

}


// Powerpoint 04
// ANTR genereer een listener interface en
// default implementatie. de icssbaselistener
// is de implementatie en deze heeft de interface
// de icsslistener. We maken zelf de aSTListener.
// Ons doel hiervoor is om de properties te verzamelen
// in een 'handige hashtabel'.