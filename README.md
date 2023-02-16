# jena_reasoner
Try to understand how ontology integration works in [Apache Jena](https://jena.apache.org/).

Two possibilities are investigated in this project:
- via [Apache Jena Fuseki](https://jena.apache.org/documentation/fuseki2/index.html), a SPARQL server
- via Apache Jena API, a Java API

## 1. Data

Two samples with corresponding ontology data are tested:

|         | Sample data        | Ontology                     |
|:-------:|:------------------:|:----------------------------:|
| Pizza   | `data/pizza.ttl`   | `data/pizza_ontology.rdf`    |
| Biology | `data/biology.ttl` | `data/biolink-model.owl.ttl` |

### 1.1. Pizza

We focus on the inverse relationships defiend in OWL: 

    :isIngredientOf owl:inverseOf :hasIngredient .

i.e. the property `isIngredientOf` is the inverse of the property `hasIngredient` 
(see `data/pizza_ontology.rdf`).

Two pizza `Margherita` and `Cheese Bacon`: the relationship between a pizza and 
an ingredient is declared by the property either `isIngredientOf` or `hasIngredient` 
(see `data/pizza.ttl`). In summary:

- Margherita contains tomato and basilic
- Cheese Bacon contains cheese, bacon, and tomato

### 1.2. Biology

[Biolink Model](https://biolink.github.io/biolink-model/) is a high level data model of 
biological entities and their associations in [SWRL: a Semantic Web Rule Language
Combining OWL and RuleML](https://www.w3.org/Submission/SWRL/).  

We focus on the inverse relationships: 

    biolink:catalyzes owl:inverseOf biolink:has_catalyst .

i.e. the property `biolink:catalyzes` is the inverse of the property `biolink:has_catalyst`
(see `data/biolink-model.owl.ttl`).

Three biological reactions have corresponding catalysts (enzymes) and their relationships are
declared by the property either `biolink:catalyzes` or `biolink:has_catalyst`
(see `data/biology.ttl`). In summary:

- ReactionA is catalyzed by EnzymeA1
- ReactionB is catalyzed by EnzymeB1 and EnzymeB2
- ReactionC is catalyzed by EnzymeC1 and EnzymeC2


## 2. Expected result

Initial missing information is complemented by ontology transitivity.  

e.g. for Pizza, after a SPARQL query asking all triples `?pizza pizza:hasIngredient ?ingredient`, 
initial missing information will be complemented because `ex:tomato pizza:isIngredientOf ex:margherita`
is equivalent to `ex:margherita pizza:hasIngredient ex:tomato`.


## 3. Method

### 3.1. Fuseki

Each type of data (Pizza, Biology) is uploaded on [TDB](https://jena.apache.org/documentation/tdb/index.html), 
a component of Jena for semantic data storage and query.  
e.g. `pizza_ontology.rdf` is saved in `/opt/tomcat/fuseki/base/databases/test_ontology_pizza`.


Then a Fuseki's configuration is done according to 
[Fuseki: Configuring Fuseki](https://jena.apache.org/documentation/fuseki2/fuseki-configuration.html)
(section Inference) to integrate ontology.
The configuration is the same as `data/config.ttl`.

All possible reasoners are tested:
- Generic Rule Reasoner: `http://jena.hpl.hp.com/2003/GenericRuleReasoner`
- Transitive Reasoner: `http://jena.hpl.hp.com/2003/TransitiveReasoner`
- RDFS Rule Reasoner: `http://jena.hpl.hp.com/2003/RDFSExptRuleReasoner`
- Full OWL Reasoner: `http://jena.hpl.hp.com/2003/OWLFBRuleReasoner`
- Mini OWL Reasoner: `http://jena.hpl.hp.com/2003/OWLMiniFBRuleReasoner`
- Micro OWL Reasoner: `http://jena.hpl.hp.com/2003/OWLMicroFBRuleReasoner`

**Note:** there is no reasoner for SWRL. 

### 3.2. Jena API

Java codes are in `src/Pizza.java` and `src/Biology.java`.  
For both case Jena used Full OWL Reasoner (`OWLFBRuleReasoner`).

## 4. Results

### 4.1. Fuseki

No ontology integration. Only raw information is retrieved:

```
# Pizza
[http://example.com/cheese_bacon, http://example.com/pizzas.owl#hasIngredient, http://example.com/tomato]
[http://example.com/cheese_bacon, http://example.com/pizzas.owl#hasIngredient, http://example.com/bacon]
[http://example.com/margherita, http://example.com/pizzas.owl#hasIngredient, http://example.com/basilic]
```

### 4.2. Jena API

The expected results are achieved.  

```
--- In raw input pizza data ---
hasIngredient property
[http://example.com/cheese_bacon, http://example.com/pizzas.owl#hasIngredient, http://example.com/tomato]
[http://example.com/cheese_bacon, http://example.com/pizzas.owl#hasIngredient, http://example.com/bacon]
[http://example.com/margherita, http://example.com/pizzas.owl#hasIngredient, http://example.com/basilic]
isIngredientOf property
[http://example.com/cheese, http://example.com/pizzas.owl#isIngredientOf, http://example.com/cheese_bacon]
[http://example.com/tomato, http://example.com/pizzas.owl#isIngredientOf, http://example.com/margherita]

--- Results of SPARQL on raw model ---
( ?s = <http://example.com/cheese_bacon> ) ( ?o = <http://example.com/bacon> )
( ?s = <http://example.com/cheese_bacon> ) ( ?o = <http://example.com/tomato> )
( ?s = <http://example.com/margherita> ) ( ?o = <http://example.com/basilic> )

Binding ontology schema
    reasoner: org.apache.jena.reasoner.rulesys.OWLFBRuleReasoner@65004ff6
--- Results of SPARQL on inferred model ---
( ?s = <http://example.com/cheese_bacon> ) ( ?o = <http://example.com/bacon> )
( ?s = <http://example.com/cheese_bacon> ) ( ?o = <http://example.com/cheese> )
( ?s = <http://example.com/cheese_bacon> ) ( ?o = <http://example.com/tomato> )
( ?s = <http://example.com/margherita> ) ( ?o = <http://example.com/basilic> )
( ?s = <http://example.com/margherita> ) ( ?o = <http://example.com/tomato> )
```


```
--- In raw input biology data ---
has_catalyst property
[http://example.com/reactionB, https://w3id.org/biolink/vocab/has_catalyst, http://example.com/enzymeB1]
[http://example.com/reactionA, https://w3id.org/biolink/vocab/has_catalyst, http://example.com/enzymeA1]
catalyzes property
[http://example.com/enzymeC2, https://w3id.org/biolink/vocab/catalyzes, http://example.com/reactionC]
[http://example.com/enzymeC1, https://w3id.org/biolink/vocab/catalyzes, http://example.com/reactionC]
[http://example.com/enzymeB2, https://w3id.org/biolink/vocab/catalyzes, http://example.com/reactionB]

--- Results of SPARQL on raw model ---
( ?s = <http://example.com/reactionA> ) ( ?o = <http://example.com/enzymeA1> )
( ?s = <http://example.com/reactionB> ) ( ?o = <http://example.com/enzymeB1> )

Binding ontology schema
    reasoner: org.apache.jena.reasoner.rulesys.OWLFBRuleReasoner@1ddd3478
    duration: 00:00:59
Creating inference model
    duration: 00:00:00
--- Results of SPARQL on inferred model ---
( ?s = <http://example.com/reactionA> ) ( ?o = <http://example.com/enzymeA1> )
( ?s = <http://example.com/reactionB> ) ( ?o = <http://example.com/enzymeB1> )
( ?s = <http://example.com/reactionB> ) ( ?o = <http://example.com/enzymeB2> )
( ?s = <http://example.com/reactionC> ) ( ?o = <http://example.com/enzymeC1> )
( ?s = <http://example.com/reactionC> ) ( ?o = <http://example.com/enzymeC2> )
    duration: 00:00:00
```

## 5. Conclusion

- The ontology inference was not successful on Fuseki server. 
  - The configuration file is not correctly applied. 
  - **I need advice from the experts...!!!**
- Inference was successful via Jena API.
  - For a big ontology data (Biolink Model), the step of `bindSchema()` is slow (around 1 minute)
  - Full OWL Reasoner seems working on Biolink Model (SWRL format).

### Future work
- Merge/bind multiple ontology files to get a one ontology model
- Save ontology model (then read it when needed)
- Investigate whether Full OWL Reasoner reasoner application loses information of SWRL format