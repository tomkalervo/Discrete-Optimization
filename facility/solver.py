#!/usr/bin/python
# -*- coding: utf-8 -*-

from collections import namedtuple
import math
import mip_solver, lp_solver
import random

Point = namedtuple("Point", ['x', 'y'])
Facility = namedtuple("Facility", ['index', 'setup_cost', 'capacity', 'location'])
Customer = namedtuple("Customer", ['index', 'demand', 'location'])

def simulated_annealing(obj_value, solution, data, temperature, cooling_rate):
    def get_obj_value(solution):
        used = [0]*data["num_facilities"]
        for f in solution:
            used[f] = 1
        obj_value = sum(data["setup_costs"][f] if v == 1 else 0 for f,v in enumerate(used))
        obj_value += sum(data["distances"][f][c] for c,f in enumerate(solution))
        return obj_value
    def feasible_solution(solution):
        used = [0]*data["num_facilities"]
        for c,f in enumerate(solution):
            used[f] += data["demands"][c]
        for f,demand in enumerate(used):
            if demand > data["capacities"][f]:
                return False
        return True
    def acceptance_probability(old_value, new_value, temperature):
        if new_value < old_value:
            return 1.0
        return math.exp((old_value - new_value) / temperature)
    
    while temperature > 1:
        new_solution = solution.copy()
        c = random.randint(0, data["num_customers"] - 1)
        f = random.randint(0, data["num_facilities"] - 1)
        new_solution[c] = f

        while not feasible_solution(new_solution):
            f = random.randint(0, data["num_facilities"] - 1)
            new_solution[c] = f

        new_value = get_obj_value(new_solution)
        probability = acceptance_probability(obj_value, new_value, temperature)

        if random.random() < probability:
            obj_value = new_value
            solution = new_solution

        temperature *= 1 - cooling_rate
        
    return obj_value, solution

def length(point1, point2):
    return math.sqrt((point1.x - point2.x)**2 + (point1.y - point2.y)**2)

def solve_it(input_data):
    # Modify this code to run your optimization algorithm

    # parse the input
    lines = input_data.split('\n')

    parts = lines[0].split()
    facility_count = int(parts[0])
    customer_count = int(parts[1])
    
    facilities = []
    for i in range(1, facility_count+1):
        parts = lines[i].split()
        facilities.append(Facility(i-1, float(parts[0]), int(parts[1]), Point(float(parts[2]), float(parts[3])) ))
    # print(facilities)
    customers = []
    for i in range(facility_count+1, facility_count+1+customer_count):
        parts = lines[i].split()
        customers.append(Customer(i-1-facility_count, int(parts[0]), Point(float(parts[1]), float(parts[2]))))
    
    # build a trivial solution
    # pack the facilities one by one until all the customers are served
    def trivial_solution():
        solution = [-1]*len(customers)
        capacity_remaining = [f.capacity for f in facilities]

        facility_index = 0
        for customer in customers:
            if capacity_remaining[facility_index] >= customer.demand:
                solution[customer.index] = facility_index
                capacity_remaining[facility_index] -= customer.demand
            else:
                facility_index += 1
                assert capacity_remaining[facility_index] >= customer.demand
                solution[customer.index] = facility_index
                capacity_remaining[facility_index] -= customer.demand

        used = [0]*len(facilities)
        for facility_index in solution:
            used[facility_index] = 1

        # calculate the cost of the solution
        obj = sum([f.setup_cost*used[f.index] for f in facilities])
        for customer in customers:
            obj += length(customer.location, facilities[solution[customer.index]].location)

        # prepare the solution in the specified output format
        output_data = '%.2f' % obj + ' ' + str(0) + '\n'
        output_data += ' '.join(map(str, solution))
        return output_data
    
    #########################################################################
    #       Author Tom Karlsson, December 2023                              #
    #       Created using or-tools from Google                              #
    #########################################################################
    def solve():
        result = None
        print(f"Facilities: {facility_count}, customers: {customer_count}")
        data = mip_solver.create_data_model(facilities, customers, length)
        if facility_count * customer_count < 200:
            print("Using SAT for MIP-solution")
            result = mip_solver.main(data)
            
        if result == None:
            print("Using linear relaxation")
            lp_model, variables = lp_solver.create_model(data)
            status, lp_model, solver = lp_solver.solve(lp_model)
            fractions_x,fractions_y = lp_solver.get_fractions(solver,variables)
            solution = [-1 for _ in range(data["num_customers"])]
            facilities_capacity = []
            while fractions_y:
                f1,_ = fractions_y.pop()
                cap = data["capacities"][f1]
                unused = []
                while -1 in solution and fractions_x:
                    fc,v = fractions_x.pop()
                    (f2,c) = fc
                    if f2 == f1:
                        dem = data["demands"][c]
                        if solution[c] == -1:
                            if dem <= cap:
                                solution[c] = f1
                                cap -= dem
                            else:
                                unused.append((fc,v))
                    elif solution[c] == -1:
                        unused.append((fc,v))
                fractions_x.extend(unused)
                facilities_capacity.append((f1,cap))
            customers_left = []
            fractions_x.reverse()
            while fractions_x:
                (f,c),v = fractions_x.pop()
                cap = data["capacities"][f]
                index = -1
                for i,(f2,cap2) in enumerate(facilities_capacity):
                    if f == f2:
                        cap = cap2
                        index = i
                        break
                if cap - data["demands"][c] >= 0:
                    solution[c] = f
                    facilities_capacity[index] = (f,cap - data["demands"][c])
                else:
                    if c not in customers_left:
                        customers_left.append(c)
                
            for c in customers_left:
                if solution[c] != -1:
                    continue
                else:
                    index = -1
                    for i,(f,cap) in enumerate(facilities_capacity):
                        if cap - data["demands"][c] >= 0:   
                            solution[c] = f
                            index = i
                            break
                    facilities_capacity[index] = (f,cap - data["demands"][c])
            assert(not -1 in solution)
            if len(facilities_capacity) > 1:
                facilities_capacity.reverse()
                f1,cap_left1 = facilities_capacity.pop()
                while facilities_capacity:
                    f2,cap_left2 = facilities_capacity.pop()
                    cap_used2 = data["capacities"][f2]-cap_left2
                    if cap_used2 <= cap_left1: #customers can be served by f1
                        sum1 = sum(data["distances"][f1][c] if f == f2 else 0 for c,f in enumerate(solution))
                        sum2 = sum(data["distances"][f2][c] if f == f2 else 0 for c,f in enumerate(solution))
                        sum2 += data["setup_costs"][f2]
                        if sum1 < sum2:
                            solution = [f1 if f == f2 else f for f in solution]
                            cap_left1 -= cap_used2
                        else:
                            f1,cap_left1 = f2,cap_left2
                    else:
                        f1,cap_left1 = f2,cap_left2
                        
            used = [0]*data["num_facilities"]
            for f in solution:
                used[f] = 1
            obj_value = sum(data["setup_costs"][f] if v == 1 else 0 for f,v in enumerate(used))
            obj_value += sum(data["distances"][f][c] for c,f in enumerate(solution))
            result = (0,obj_value,solution)
        
        optimal, obj, solution = result
        if optimal == 0:
            print(f"Running simulated annealing, objective value before {obj}")
            temperature = 1000
            cooling_rate = 0.003
            new_obj, new_solution = simulated_annealing(obj, solution, data, temperature, cooling_rate)
            failures = 0
            while failures < 5:
                print("New objective value ", new_obj, " failures so far ", failures)
                if new_obj < obj:
                    obj = new_obj
                    solution = new_solution
                    temperature = 1000
                    cooling_rate = 0.003
                    failures = 0
                else:
                    failures += 1
                    temperature *= 2
                    cooling_rate /= 2
                    
                new_obj, new_solution = simulated_annealing(obj, solution, data, temperature, cooling_rate)
        
        # prepare the solution in the specified output format
        output_data = '%.2f' % obj + ' ' + str(optimal) + '\n'
        output_data += ' '.join(map(str, solution))
        
        file = open('test2.txt', 'a')
        file.write(output_data)
        file.write('\n')
        file.close()
        return output_data

    def print_solution():
        file = open('redirect.txt', 'r')
        output_data = file.readline()
        output_data += file.readline()
        temp_storage = [line for line in file]
        file.close()
        file = open('redirect.txt', 'w')
        for line in temp_storage:
            file.write(line)
        file.close()
        return output_data[:-1]
        
    # return solve()
    return print_solution()


import sys

if __name__ == '__main__':
    import sys
    if len(sys.argv) > 1:
        file_location = sys.argv[1].strip()
        with open(file_location, 'r') as input_data_file:
            input_data = input_data_file.read()
        print(solve_it(input_data))
    else:
        print('This test requires an input file.  Please select one from the data directory. (i.e. python solver.py ./data/fl_16_2)')

