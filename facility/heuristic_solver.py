def solve(data):
    customers = [-1 for _ in range(data["num_customers"])]
    used = [0]*data["num_facilities"]
    ratio_cap = [(data["setup_costs"][i]/data["capacities"][i],i) for i in range(data["num_facilities"])]
    ratio_cap.sort(reverse = True)
    while -1 in customers:
        r = ratio_cap.pop()
        _,f = r
        cap = data["capacities"][f]
        ratio_dem = [(data["distances"][f][i]/data["demands"][i],i) for i in range(data["num_customers"])]
        ratio_dem.sort(reverse = True)
        used_f = None
        used_c = []
        while ratio_dem:
            _,c = ratio_dem.pop()
            dem = data["demands"][c]
            if customers[c] == -1 and dem <= cap:
                used_c.append((f,c))
                used_f = 1
                cap -= dem
                
        if ratio_cap:
            r2 = ratio_cap.pop()
            _,f2 = r2
            cap = data["capacities"][f2]
            ratio_dem = [(data["distances"][f2][i],i) for i in range(data["num_customers"])]
            ratio_dem.sort(reverse = True)
            used_f2 = None
            used_c2 = []
            while ratio_dem:
                _,c = ratio_dem.pop()
                dem = data["demands"][c]
                if customers[c] == -1 and dem <= cap:
                    used_c2.append((f2,c))
                    used_f2 = 1
                    cap -= dem
            if used_f2 == 1:
                if used_f == None:
                    used_f = used_f2
                    used_c = used_c2
                    ratio_cap.append(r)
                else:
                    cost1 = sum(data["distances"][f][c] for f,c in used_c)
                    cost1 += data["setup_costs"][f]
                    cost2 = sum(data["distances"][f][c] for f,c in used_c2)
                    cost2 += data["setup_costs"][f2]
                    if cost1 <= cost2:
                        ratio_cap.append(r2)
                    else:
                        used_f = used_f2
                        used_c = used_c2
                        ratio_cap.append(r)
            else:
                ratio_cap.append(r2)
            
        if used_f == 1:
            used[f] = 1
            for f,c in used_c:
                customers[c] = f
                
    obj_value = sum(used[f]*data["setup_costs"][f] for f in range(data["num_facilities"]))
    obj_value += sum(data["distances"][customers[c]][c] for c in range(data["num_customers"]))
    return (0,obj_value,customers)