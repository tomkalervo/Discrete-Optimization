# Author Tom Karlsson, December 2023
# Code is inspired from https://github.com/google/or-tools/tree/stable/ortools/linear_solver/samples
import sys
from ortools.linear_solver import pywraplp

def create_data_model(facilities, customers, length_fun):
    """Stores the data for the problem."""
    data = {}
    data["num_facilities"] = len(facilities)
    data["num_customers"] = len(customers)
    data["demands"] = [c.demand for c in customers]
    data["capacities"] = [f.capacity for f in facilities]
    data["setup_costs"] = [f.setup_cost for f in facilities]
    data["distances"] = [
        [length_fun(f.location,c.location) for c in customers] for f in facilities
    ]
    return data

def main(data):
    # Create the mip solver with the SCIP, SAT or GLOP backend.
    solver = pywraplp.Solver.CreateSolver("SAT")
    if not solver:
        return

    # Variables
    # [START variables]
    # x[f, c] is an array of 0-1 variables, which will be one
    # if the customer is assigned to the facility.
    x = {}
    for f in range(data["num_facilities"]):
        for c in range(data["num_customers"]):
            x[f, c] = solver.BoolVar(f"x[{f},{c}]")
            
    # y[f] is an array of 0-1 variables, which will be one
    # if the facility is open (thus results in setup cost)
    y = {}
    for f in range(data["num_facilities"]):
        y[f] = solver.BoolVar(f"y[{f}]")
    # [END variables]
    
    # Constraints
    # [START constraints]
    # The total size of the demand each facility takes on is at most total_size_max if open, zero if closed.
    for f in range(data["num_facilities"]):
        solver.Add(solver.Sum([x[f, c] * data["demands"][c] for c in range(data["num_customers"])]) <= data["capacities"][f] * y[f])
        
    # Each customer is assigned to exactly one facility.
    for c in range(data["num_customers"]):
        solver.Add(solver.Sum([x[f, c] for f in range(data["num_facilities"])]) == 1)
    # [END constraints]

    # Objective
    # [START objective]
    objective_terms = []
    for f in range(data["num_facilities"]):
        for c in range(data["num_customers"]):
            objective_terms.append(data["distances"][f][c] * x[f, c])
    for f in range(data["num_facilities"]):
        objective_terms.append(data["setup_costs"][f] * y[f])
    solver.Minimize(solver.Sum(objective_terms))
    # [END objective]
    
    # Hint from heuristic solution - Not used
    #  - Sadly poorly explained in documentation
    #
    # _,_,res_customers = result
    # hint_variables_x = [x[f, c] for c, f in enumerate(res_customers)]
    # hint_values_x = {x[f, c]: 1 for c, f in enumerate(res_customers)}

    # hint_values_y = {y[f]: 1 for f in res_customers}
    # solver.SetHint(hint_variables_x,hint_values_x)
    # solver.SetHint(hint_values_y)
    # for c in range(len(res_customers)):
    #     f = res_customers[c]
    #     solver.SetHint(x[f, c], 1) 
    #     solver.SetHint(y[f], 1)

    # Solve
    # [START solve]
    # print(f"Solving with {solver.SolverVersion()}")
    # Sets a time limit of 5 minutes.
    time_limit = 30 * 60 * 1000
    solver.SetTimeLimit(time_limit)
    solver.SetNumThreads(8)
    solver.EnableOutput()
    solver.SetSolverSpecificParametersAsString("mip_max_activity_exponent:60")
    # solver.SuppressOutput()
    status = solver.Solve()
    # [END solve]

    # Print solution.
    # [START print_solution]
    def print_solution():
        if status == pywraplp.Solver.OPTIMAL or status == pywraplp.Solver.FEASIBLE:
            print(f"Total cost = {solver.Objective().Value()}, optimal? {status == pywraplp.Solver.OPTIMAL}, feasible? {status == pywraplp.Solver.FEASIBLE}\n")
            for f in range(data["num_facilities"]):
                for c in range(data["num_customers"]):
                    if x[f, c].solution_value() > 0.5:
                        dist = data["distances"][f][c]
                        print(
                            f"facility {f} assigned to customer {c}."
                            + f" distance: {dist}"
                        )
        else:
            print("No solution found.")
    print_solution()
    # [END print_solution]
    
    # [START return_solution]
    if status == pywraplp.Solver.OPTIMAL or status == pywraplp.Solver.FEASIBLE:
        objective_value = solver.Objective().Value()
        objective_customers = [-1 for _ in range(data["num_customers"])]
        for f in range(data["num_facilities"]):
            for c in range(data["num_customers"]):
                if x[f, c].solution_value() > 0:
                    print(f"x[f, c].solution_value() = {x[f, c].solution_value()}")
                if x[f, c].solution_value() > 0.5:
                    objective_customers[c] = f
        return (1-status, objective_value, objective_customers)
    else:
        return None
    # [END return_solution]
