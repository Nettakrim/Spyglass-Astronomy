#https://www.johndcook.com/blog/2022/11/02/keplers-equation-python/
from numpy import sqrt, cbrt, pi, sin, cos, arcsin, arctan

# This will solve the special form of the cubic we need.
def solve_cubic(a, c, d):
    assert(a > 0 and c > 0)
    p = c/a
    q = d/a
    k = sqrt( q**2/4 + p**3/27 )
    return cbrt(-q/2 - k) + cbrt(-q/2 + k)

# Machin's starting point for Newton's method
# See johndcook.com/blog/2022/11/01/kepler-newton/
def machin(e, M):
    n = sqrt(5 + sqrt(16 + 9/e))
    a = n*(e*(n**2 - 1)+1)/6
    c = n*(1-e)
    d = -M
    s = solve_cubic(a, c, d)
    return n*arcsin(s)    

def solve_kepler(e, M):
    "Find E such that M = E - e sin E."
    if (e == 0): return M
    assert(0 <= e < 1)
    assert(0 <= M <= pi) 
    f = lambda E: E - e*sin(E) - M 
    E = machin(e, M) 
    tolerance = 1e-10 
    # Newton's method 
    while (abs(f(E)) > tolerance):
        E -= f(E)/(1 - e*cos(E))
    return E

def get_angle(e, E):
    beta = e/(1+sqrt(1-e*e))
    return E+2*arctan((beta*sin(E))/(1-beta*cos(E)))

s=""
for y in range(0,32):
    e = y/32
    if e == 1:
        e = 0.99
    s+="\nnew float[] {"
    for x in range(0,32):
        M = (x/31)*pi
        f = get_angle(e,solve_kepler(e, M))
        s+=str(f)+"f, "
    s=s[:-2]
    s+="},"
s=s[:-1]
print(s)