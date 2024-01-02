# Author Tom Karlsson, December 2023
# Code is inspired from https://github.com/google/or-tools/tree/stable/ortools/linear_solver/samples
# This program returns linear relaxation to MIP-problem
import sys
from ortools.linear_solver import pywraplp
from ortools.linear_solver.python import model_builder

#Create the model from data
def create_model(data):
    # [START model]
    # Create the model.
    model = model_builder.Model()
    # [END model]
        
    # Variables
    # [START variables]
    # x[f, c] is an array of 0-1 variables, which will be one
    # if the customer is assigned to the facility.
    x = {}
    for f in range(data["num_facilities"]):
        for c in range(data["num_customers"]):
            x[f, c] = model.new_int_var(0.0,1.0,f"x[{f},{c}]")
            
    # y[f] is an array of 0-1 variables, which will be one
    # if the facility is open (thus results in setup cost)
    y = {}
    for f in range(data["num_facilities"]):
        y[f] = model.new_int_var(0.0,1.0,f"y[{f}]")
    # [END variables]
    
    # Constraints
    # [START constraints]
    # The total size of the demand each facility takes on is at most total_size_max if open, zero if closed.
    for f in range(data["num_facilities"]):
        model.add(sum([x[f, c] * data["demands"][c] for c in range(data["num_customers"])]) <= data["capacities"][f] * y[f])
        
    # Each customer is assigned to exactly one facility.
    for c in range(data["num_customers"]):
        model.add(sum([x[f, c] for f in range(data["num_facilities"])]) == 1)
    # [END constraints]

    # Objective
    # [START objective]
    objective_terms = []
    for f in range(data["num_facilities"]):
        for c in range(data["num_customers"]):
            objective_terms.append(data["distances"][f][c] * x[f, c])
    for f in range(data["num_facilities"]):
        objective_terms.append(data["setup_costs"][f] * y[f])
    model.minimize(sum(objective_terms))
    # [END objective]
    
    return (model, (x,y))
    
# Solve the model
def solve(model):
    solver = model_builder.Solver("GLOP")
    if not solver:
        print("Could not create solver")
        sys.exit(1)
    # [START solve]
    solver.enable_output(False)
    status = solver.solve(model)
    # [END solve]
    return status, model, solver

def get_fractions(solver, variables):
    x,y = variables
    fractions_x = []
    fractions_y = []
    for fc in x:
        # print(f"x[{fc}], {x[fc].solution_value()}")
        if  0 < solver.value(x[fc]) <= 1:
            # print(f"solver.value(x[fc]) {solver.value(x[fc])}")
            fractions_x.append((fc,solver.value(x[fc])))
    for f in y:
        if 0 < solver.value(y[f]) <= 1:
            # print(f"solver.value(y[f]) {solver.value(y[f])}")
            fractions_y.append((f,solver.value(y[f])))
    fractions_x.sort(key=lambda x: x[1])
    fractions_y.sort(key=lambda x: x[1])
    return fractions_x,fractions_y
    
def is_optimal(status):
    return status == pywraplp.Solver.OPTIMAL
def is_feasible(status):
    return status == pywraplp.Solver.FEASIBLE

def get_solution(status, solver, data, variables):
    status = 1 if is_optimal(status) else 0
    x,y = variables
    # [START return_solution]
    if status == pywraplp.Solver.OPTIMAL or status == pywraplp.Solver.FEASIBLE:
        objective_value = solver.objective_value
        objective_customers = [-1 for _ in range(data["num_customers"])]
        for f in range(data["num_facilities"]):
            for c in range(data["num_customers"]):
                if solver.value(x[f, c]) == 1.0:
                    objective_customers[c] = f
        return (status, objective_value, objective_customers)
    else:
        return None
    # [END return_solution]
    
def add_constraint(decision_x,value,model):
    c = model.add(decision_x == value)
    return c, model
