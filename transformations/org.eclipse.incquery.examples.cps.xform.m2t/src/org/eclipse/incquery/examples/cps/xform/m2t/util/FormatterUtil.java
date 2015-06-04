package org.eclipse.incquery.examples.cps.xform.m2t.util;

import org.eclipse.incquery.examples.cps.xform.m2t.exceptions.CPSGeneratorException;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

import com.google.common.base.CaseFormat;

public class FormatterUtil {

	
	public String formatCode(CharSequence code) throws CPSGeneratorException{
		CodeFormatter formatter = ToolFactory.createCodeFormatter(null);
		
		TextEdit textEdit = formatter.format(CodeFormatter.K_COMPILATION_UNIT, code.toString(), 0, code.length(), 0, null);
		
		if(textEdit == null){
			// TODO handle...
			return code.toString();
		}
		
		IDocument doc = new Document(code.toString());
		try {
			textEdit.apply(doc);
		} catch (MalformedTreeException e) {
			throw new CPSGeneratorException("Cannot complete codeformatting", e);
		} catch (BadLocationException e) {
			throw new CPSGeneratorException("Cannot complete codeformatting", e);
		}
		
		return doc.get();
	}
	
	public String purify(String string){
        return string.replaceAll("[^A-Za-z0-9]", "");
    }
    
    public String purifyAndToUpperCamel(String string){
        String str = purify(string.replace(' ', '_').toLowerCase());
        return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, str);
    }
}
