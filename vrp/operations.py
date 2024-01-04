# Author Tom Karlsson, January 2024
# Uses the ortools CVRP solver to find routes for capacity vehicle routing problem
import sys,random,math
from ortools.constraint_solver import routing_enums_pb2
from ortools.constraint_solver import pywrapcp
import cvrp_ortools

def create_data_model(customer_count,vehicle_count,vehicle_capacity,customers,depot,length):
    """Stores the data for the problem."""
    data = {}
    data["distance_matrix"] = [
        [length(c1,customers[c2]) for c1 in customers] for c2 in range(customer_count)
    ]
    data["demands"] = [c.demand for c in customers]
    data["vehicle_capacities"] = [vehicle_capacity for _ in range(vehicle_count)]
    data["num_vehicles"] = vehicle_count
    data["depot"] = depot.index
    return data

def get_initial_solution(data):
    customers_left = [*enumerate(data["demands"])]
    customers_left.pop(0)
    customers_left = sorted(customers_left, key=lambda x: x[1], reverse=True)
    solution = []
    for v in range(data["num_vehicles"]):
        capacity = data["vehicle_capacities"][v]
        i = 0
        route = [i]
        while i < len(customers_left):
            c,d = customers_left[i]
            if capacity - d >= 0:
                route.append(c)
                capacity -= d
                customers_left.pop(i)
            else:
                i += 1
        route.append(0)
        solution.append(route)
    assert(len(customers_left) == 0) # Only works on feasible data input
    return solution

def obj(routes,data):
    total_distance = 0
    for route in routes:
        for i in range(1,len(route)):
            c_from = route[i-1]
            c_to = route[i]
            total_distance += data["distance_matrix"][c_from][c_to]
    return total_distance

def simulated_anneling(solution,data,temperature):
    def capacity_left(route):
        max_capacity = data["vehicle_capacities"][0]
        used_capacity = sum(data["demands"][c] for c in route)
        return max_capacity - used_capacity
        
    def insert_into(matrix,element,row):
        queue = [element]
        while queue:
            element = queue.pop(0)
            random.shuffle(matrix)
            inserted = False
            for other_row in matrix:
                if other_row != row and capacity_left(other_row) - data["demands"][element] >= 0:
                    available_positions = list(range(1, len(other_row) - 1)) 
                    assert(available_positions)
                    # Choose a random position from the available positions
                    random_position = random.choice(available_positions)
                    other_row.insert(random_position, element)
                    inserted = True
                    break
                
            if not inserted:
                new_row = random.choice(matrix)
                while new_row == row:
                    new_row = random.choice(matrix)
                row = new_row
                capacity = capacity_left(row)
                while capacity - data["demands"][element] < 0:
                    e = random.choice(row[1:-1])
                    row.remove(e)
                    capacity += data["demands"][e]
                    queue.append(e)
                    
                available_positions = list(range(1, len(row) - 1))
                assert(available_positions)
                # Choose a random position from the available positions
                random_position = random.choice(available_positions)

                # Insert a new element at the chosen position
                row.insert(random_position, element)

        return matrix
            
    def acceptance_probability(old_value, new_value, temperature):
        if new_value < old_value:
            return 1.0
        return math.exp((old_value - new_value) / temperature)
    
    current_value = obj(solution,data)
    cooling_rate = 0.003
    while temperature > 1:
        # print(f"Temp: {temperature}")
        new_solution = []
        for row in solution:
            new_solution.append(row.copy())     
        # Pick a random row
        row = random.choice(new_solution)
        # Choose a random element from the row excluding the first and last
        element_to_move = random.choice(row[1:-1])
        row.remove(element_to_move)
        
        # Shift around elements until a feasible permutation is found
        new_solution = insert_into(new_solution,element_to_move,row)
        # Calculate constants and probability
        new_value = obj(new_solution,data)
        probability = acceptance_probability(current_value,new_value,temperature)

        # Metropolis criterion for accepting the new solution
        if random.random() < probability:
            current_value = new_value
            solution = new_solution

        # Cooling process
        temperature *= 1 - cooling_rate
            
    return current_value,solution
            
def start_search(customer_count,vehicle_count,vehicle_capacity,customers,depot,length):
    results = None
    
    # Instantiate the data problem.
    data = create_data_model(customer_count,vehicle_count,vehicle_capacity,customers,depot,length)
    
    # adapt to ortools cvrp
    original_distance_matrix = []
    for row in data["distance_matrix"]:
        original_distance_matrix.append(row.copy())
        
    # simulate Integers
    for i in range(len(data["distance_matrix"])):
        for j in range(len(data["distance_matrix"][i])):
            data["distance_matrix"][i][j] *= 1000000
            data["distance_matrix"][i][j] = int(data["distance_matrix"][i][j])
    # timelimit = 1
    timelimit = len(data["distance_matrix"]) * 2
    status, results = cvrp_ortools.main(data,timelimit)
    data["distance_matrix"] = original_distance_matrix

    if status in [1,2]:
        manager, routing, solution = results
        # revert back to float
        return cvrp_ortools.get_solution_format(data, manager, routing, solution)
    else:
        solution = get_initial_solution(data)
        objective_value = obj(solution,data)
        temperature = 1000
        failures = 0
        new_objective_value,new_solution = simulated_anneling(solution,data,temperature)
        while failures < 2:
            if new_objective_value >= objective_value:
                failures += 1
            else:
                solution = new_solution
                objective_value = new_objective_value
            new_objective_value,new_solution = simulated_anneling(solution,data,temperature)
        return obj(solution,data), solution

    