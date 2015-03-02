BIBTEX=/usr/texbin/bibtex
DOT=/opt/local/bin/dot
JAVA=/usr/bin/java
MAKEGLOSSARIES=/usr/texbin/makeglossaries
PDFLATEX=/usr/texbin/pdflatex
PLANTUML=plantuml.jar

UML=\
	instruct.uml \
	milkorder.uml \
	newmarket.uml \
	neworder.uml \
	newtrader.uml \
	rec.uml \
	reviseorder.uml \
	serv.uml \
	state.uml \
	view.uml

EPS=$(UML:%.uml=%.eps)
PNG=$(UML:%.uml=%.png)

all: twirly.pdf

clean:
	rm -f $(EPS) $(PNG) \
	*.acn *.acr *.alg *.aux \
	*.bbl *.blg *.dvi *.glg \
	*.glo *.gls *.log *.out \
	*.pdf *.toc *.xdy *~

.SECONDARY: $(EPS)

twirly.pdf: twirly.bib twirly.tex $(EPS)
	$(PDFLATEX) twirly
	$(BIBTEX) twirly
	$(MAKEGLOSSARIES) twirly.acn
	$(MAKEGLOSSARIES) twirly.glo
	$(PDFLATEX) twirly
	$(PDFLATEX) twirly

%.eps: %.uml
	$(JAVA) -jar plantuml.jar -graphvizdot $(DOT) -teps $<

%.svg: %.uml
	$(JAVA) -jar plantuml.jar -graphvizdot $(DOT) -tsvg $<
