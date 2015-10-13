BIBTEX=/usr/texbin/bibtex
DOT=/opt/local/bin/dot
JAVA=/usr/bin/java
MAKEGLOSSARIES=/usr/texbin/makeglossaries
PDFLATEX=/usr/texbin/pdflatex
PLANTUML=plantuml.jar

UML=\
	hybrid-model.uml \
	order-entry.uml \
	order-model.uml \
	quote-model.uml

EPS=$(UML:%.uml=%.eps)
PNG=$(UML:%.uml=%.png)

all: swirly.pdf

clean:
	rm -f $(EPS) $(PNG) \
	*.acn *.acr *.alg *.aux \
	*.bbl *.blg *.dvi *.glg \
	*.glo *.gls *.log *.out \
	*.pdf *.toc *.xdy *~

.SECONDARY: $(EPS)

swirly.pdf: swirly.bib swirly.tex $(EPS)
	$(PDFLATEX) swirly
	$(BIBTEX) swirly
	$(MAKEGLOSSARIES) swirly.acn
	$(MAKEGLOSSARIES) swirly.glo
	$(PDFLATEX) swirly
	$(PDFLATEX) swirly

%.eps: %.uml
	$(JAVA) -jar plantuml.jar -graphvizdot $(DOT) -teps $<

%.png: %.uml
	$(JAVA) -jar plantuml.jar -graphvizdot $(DOT) -tpng $<
