import numpy as np
import matplotlib.pyplot as plt

def readpedigrees(fname):
    with open(fname) as f:
        content = f.readlines()
    values = []
    for t in content:
        values.append([int(x) for x in t.split()])
    maxlen = np.max([len(x) for x in values])
    pedigrees=[]
    for p in values:
        row = np.zeros(maxlen)
        row[:len(p)] = p[:len(p)]
        pedigrees.append(row)
    return np.array(pedigrees)


