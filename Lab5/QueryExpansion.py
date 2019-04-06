#%% [markdown]
# # Query Expansion
#%% [markdown]
# 0) Just some imports

#%%
import re
import math
import numpy as np
import common as cm

#%% [markdown]
# # 1) Simple search engine
#%% [markdown]
# 1.1) Get acquainted with the below class. There are several TODOs. However, DO NOT complete them now. 

#%%
class Dictionary:
    def __init__(self):
        ### keeps unique terms (SORTED!)
        self.terms = self.loadTerms("terms.txt")
        self.idfs = [] ### IDF coefficients
        self.corM = [] ### a correlation matrix

    ### load terms
    def loadTerms(self, fileName):
        file = open(fileName,'r', encoding='utf-8-sig')
        k = [self.proc(s) for s in file.readlines()]
        k.sort()
        return k

    ### ignore it
    def proc(self, s):
        if s[-1] == '\n': return s[:-1]
        else: return s
    
    ### TODO (DO NOT FINISH THIS METHOD YET. YOU WILL BE ASKED FOR IT LATER) 
    def computeIDFs(self, documents):
        self.idfs = []
    
    ### TODO (DO NOT FINISH THIS METHOD YET. YOU WILL BE ASKED FOR IT LATER) 
    def computeCorM(self, documents):
        self.corM = [[0]]
        

### SOME DEBUG
dictionary = Dictionary()
print(dictionary.terms[:50])

#%% [markdown]
# 1.2) Load files: here we load some example collection of documents. RAW_DOCUMENTS = just strings. Check if the documents are loaded correctly (e.g., print RAW_DOCUMENTS[0])

#%%
RAW_DOCUMENTS = cm.loadDocuments("documents.txt")
### SOME DEBUG
print(RAW_DOCUMENTS[0])


#%%
### SOME DEBUG, JUST RUN; check if (a) common.py is imported correctly and (b) 
### tokens are correctly derived from some document (e.g., RAW_DOCUMENTS[0])
print(cm.simpleTextProcessing(RAW_DOCUMENTS[0], re))

#%% [markdown]
# 1.3) Get acquainted with the below class. 

#%%
class Document:
    def __init__(self, doc_id, raw_document, dictionary):
        self.doc_id = doc_id ### DOC ID, simply 0,1,2,3....
        self.raw_document = raw_document ### raw data, i.e., string data
        self.dictionary = dictionary # reference to the dictionary
        
        ### DOCUMENT REPRESENTATIONS
        self.tokens = cm.simpleTextProcessing(raw_document, re) ### get terms
        self.bow = [] # Bag Of Words (BOW - number of term occurences)
        self.tf = [] # TF representation
        self.tf_idf = [] # TF-IDF representation

    ### TODO - complete this method; it should compute a BOW representation
    def computeBOW_Representation(self):
        self.bow = []
    
    ### TODO - complete this method; it should compute a TF representation
    def computeTF_Representation(self):
        self.tf = []
    
    ### TODO - complete this method; it should compute a TFxIDF representation 
    ### (important: it should not be run before dictionary.idfs are not computed!)
    def computeTF_IDF_Representation(self):
        self.tf_idf = []
    
    def computeRepresentations(self):
        self.computeBOW_Representation()
        self.computeTF_Representation()
        self.computeTF_IDF_Representation()
    
documents = [Document(i, RAW_DOCUMENTS[i], dictionary) for i in range(len(RAW_DOCUMENTS))]

#%% [markdown]
# 1.4) Compute IDFs here

#%%
### TODO COMPUTE IDFS HERE (FINISH THE PROPER METHOD OF THE DICTIONARY CLASS - DO NOT FORGET TO RE-RUN THE CELL)
dictionary.computeIDFs(documents)

### SOME DEBUG
res = [[dictionary.terms[i], dictionary.idfs[i]] for i in range(len(dictionary.terms))]
res.sort(key = lambda x: x[1])
# LEAST COMMON WORDS - HIGH IDF
print(res[-5:])
# MOST COMMON WORDS - LOW IDF
print(res[:5])

#%% [markdown]
# 1.5) Compute the document representations (for each document run computeRepresentations())

#%%
for d in documents: d.computeRepresentations()
### SOME DEBUG (you should see some 4s - which terms are these?)
print(documents[0].bow)

#%% [markdown]
# 1.6) Finish the below method. It should compute and return a cosine similarity (v1 and v2 are two vectors - tf-idf representations)

#%%
### TODO 
def getSimilarity(v1, v2):
    return 0.0

#%% [markdown]
# 1.7) Run the below script for different queries. getTopResults seeks for documents being the most similar/relevant to the query. Do you find the results satisfactory?

#%%
query = "machine learning"
#query = "academic research"
#query = "international conference"
#query = "international conference washington"


#%%
def getTopResults(query, documents, dictionary, similarity, top = 5):
    qd = Document(-1, query, dictionary)
    qd.computeRepresentations()
    ranks = [[d, getSimilarity(d.tf_idf, qd.tf_idf)] for d in documents]
    ranks.sort(key=lambda x: -x[1])
    for i in range(top):
        print("RANK = " + str(i+1) + " WITH SIMILARITY = " + str(ranks[i][1]) + " | DOC ID = " + str(ranks[i][0].doc_id))
        print(ranks[i][0].raw_document)
        print("")

getTopResults(query, documents, dictionary, getSimilarity, top = 5)

#%% [markdown]
# # 2) Query expansion
#%% [markdown]
# ## 2.1) Correlation matrix
#%% [markdown]
# 2.1.1) Finish dictionary.computeCorM method (see class Dictionary). It should generate a correlation matrix (correlation between terms).
# 
# IMPORTANT: although corM[ i ][ i ] (for each i) should be 1.0, set it to -1.0

#%%
### TODO - COMPLETE THE computeCorM METHOD (see one of the first cells)
dictionary.computeCorM(documents)
print(dictionary.corM)

#%% [markdown]
# 2.1.2) Finish the below method. For each term in the query (you must parse the query, see getTopResults() method), find another term which is the most correlated with the input term.

#%%
query = "machine"
#query = "algorithm"
# query = "learning"
# query = "conference"
# query = "research"
# query = "concept"

def suggestKeywords(query, dictionary):
    ### TODO
    print("Suggestions")
    pass
        
suggestKeywords(query, dictionary)

#%% [markdown]
# # 2.2) Rocchio algorithm
#%% [markdown]
# $\overrightarrow{q_{m}} = \alpha \overrightarrow{q} + \left(\beta \cdot \dfrac{1}{|D_{r}|} \sum_{\overrightarrow{D_j} \in D_{r}} \overrightarrow{D_j} \right) - \left(\gamma \cdot \dfrac{1}{|D_{nr}|} \sum_{\overrightarrow{D_j} \in D_{nr}} \overrightarrow{D_j} \right)$
#%% [markdown]
# 2.2.1) Firstly, run the below code. Observe the results. Assume that we do not like the first and the second result (Docs 63 and 77). However, assume that docs 29 and 36 are satisfactory. Now, modfify the method. It should alter the query vector, according to Rocchio algorithm. Check the result for the above considered scenario (relevant docs = 29 and 36; not relevant = 63 and 77). Check the results for different values of alpha, beta, and gamma coefficients. 

#%%
def getTopResults_Rocchio(query, 
                          documents, 
                          dictionary, 
                          similarity, 
                          rel_docs = [29, 36],
                          nrel_docs = [63, 77],
                          alpha = 0.5,
                          beta = 0.3,
                          gamma = 0.2,
                          top = 10):
    qd = Document(-1, query, dictionary)
    qd.computeRepresentations()
    ##### TODO: MODIFY qd.tf_idf HERE
    
    #####
    ranks = [[d, getSimilarity(d.tf_idf, qd.tf_idf)] for d in documents]
    ranks.sort(key=lambda x: -x[1])
    for i in range(top):
        print("RANK = " + str(i+1) + " WITH SIMILARITY = " + str(ranks[i][1]) + " | DOC ID = " + str(ranks[i][0].doc_id))
        print(ranks[i][0].raw_document)
        print("")

getTopResults_Rocchio("machine learning", documents, dictionary, getSimilarity, top = 10)

#%% [markdown]
# # 2.3) WordNet
#%% [markdown]
# 2.3.1) Installation
#%% [markdown]
# http://www.nltk.org/install.html
# 
# import nltk 
# 
# nltk.download()
# 
# https://www.nltk.org/data.html

#%%
from nltk.corpus import wordnet as wn

#%% [markdown]
# Definition: synset = (from wiki) (information science) A set of one or more synonyms that are interchangeable in some context without changing the truth value of the proposition in which they are embedded.
#%% [markdown]
# 2.3.2) Display sysents for "machine"

#%%
wn.synsets('machine')

#%% [markdown]
# 2.3.3) Display all definitions (.definition()) for synsets (machine)

#%%
#TODO

#%% [markdown]
# 2.3.4) For each synset (machine), display its hypernym (a word with a broad meaning constituting a category into which words with more specific meanings fall; a superordinate. For example, colour is a hypernym of red).

#%%
#TODO

#%% [markdown]
# See: http://www.nltk.org/howto/wordnet.html
# for more examples

#%%



