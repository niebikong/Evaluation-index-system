# 基于模糊层次分析和偏离度的无人机作战效能评估

import numpy as np

def to_consistent_matrix(matrix):
    n = len(matrix)
    ξ = np.sum(matrix, axis=1)
    Q = np.zeros((n, n))
    for i in range(n):
        for j in range(n):
            Q[i, j] = (ξ[i] - ξ[j]) / (2 * n) + 0.5
    return Q

def ranking_vector(F):
    # 计算排序向量
    n = len(F)
    a=(n-1)/2
    w_prime = np.zeros(n)
    
    for i in range(n):
        w_prime[i] = 1/n - 1/(2*a) + 1/(n*a) * np.sum(F[i])
    
    return w_prime

def calculate_deviation(A, alpha, beta):
    """
    计算矩阵 A 中每个元素相对于其所在列均值的偏离度。
    
    参数:
    A (numpy.ndarray): 输入矩阵。
    alpha (float): 偏离度计算中的参数 α。
    beta (float): 偏离度计算中的参数 β。
    
    返回:
    numpy.ndarray: 每个元素的偏离度。
    """
    m, n = A.shape
    s_j = A.mean(axis=0)
    S_ij = np.zeros_like(A)
    
    for i in range(m):
        for j in range(n):
            if A[i, j] <= s_j[j]:
                S_ij[i, j] = (alpha + s_j[j]) / (alpha + A[i, j])
            else:
                S_ij[i, j] = (beta + A[i, j]) / (beta + s_j[j])
    
    return S_ij

def calculate_efficiency_values(A, alpha_ij):
    """
    计算第 i 组评估对象的效能值。
    
    参数:
    A (numpy.ndarray): 输入矩阵。
    alpha_ij (numpy.ndarray): 动态权值矩阵。
    
    返回:
    numpy.ndarray: 效能值向量。
    """
    U_i = np.sum(alpha_ij * A, axis=1)
    return U_i

def normalize_data(data, positive_indicators=[0,1,2,3,4,5,8,19,20,23,26,27,28,29,30,31,32,33], negative_indicators=[6,7,9,10,11,12,13,14,15,16,17,18,21,22,24,25]):
    normalized_data = np.zeros_like(data)
    rows, cols = data.shape
    
    # 处理正指标
    for i in positive_indicators:
        xmin = np.min(data[i, :])
        xmax = np.max(data[i, :])
        for j in range(cols):
            if xmax - xmin != 0:
                normalized_data[i, j] = (data[i, j] - xmin) / (xmax - xmin)
            else:
                normalized_data[i, j] = 1

    # 处理负指标
    for i in negative_indicators:
        xmin = np.min(data[i, :])
        xmax = np.max(data[i, :])
        for j in range(cols):
            if xmax - xmin != 0:
                normalized_data[i, j] = (xmax - data[i, j]) / (xmax - xmin)
            else:
                normalized_data[i, j] = 1

    return normalized_data

if __name__ == '__main__':
    # 构造判断矩阵
    # R_1 = np.full((7, 7), 0.5)
    R_1 = [
        [0.5,0.6,0.7,0.7,0.6,0.6,0.9],
        [0.4,0.5,0.8,0.7,0.6,0.6,0.9],
        [0.3,0.2,0.5,0.5,0.5,0.5,0.9],
        [0.3,0.3,0.5,0.5,0.5,0.5,0.5],
        [0.4,0.4,0.5,0.5,0.5,0.5,0.5],
        [0.4,0.4,0.5,0.5,0.5,0.5,0.5],
        [0.1,0.1,0.1,0.5,0.5,0.5,0.5]
    ]

    R_2 = [
    [0.5, 0.55, 0.6, 0.7, 0.8, 0.9],
    [0.45, 0.5, 0.55, 0.6, 0.7, 0.8],
    [0.4, 0.45, 0.5, 0.55, 0.6, 0.7],
    [0.3, 0.4, 0.45, 0.5, 0.55, 0.6],
    [0.2, 0.3, 0.4, 0.45, 0.5, 0.55],
    [0.1, 0.2, 0.3, 0.4, 0.45, 0.5],
    ]

    R_3 = [
    [0.5, 0.2, 0.1],
    [0.8, 0.5, 0.3],
    [0.9, 0.7, 0.5]
    ]

    R_4 = np.full((3, 3), 0.5)
    R_5 = np.full((7, 7), 0.5)
    R_6 = np.full((8, 8), 0.5)
    R_7 = np.full((4, 4), 0.5)
    R_8 = np.full((3, 3), 0.5)

    # A = [
    # [0.63, 0.47, 0.35, 0.78, 0.31, 0.27, 0.56, 0.74, 0.51, 0.76, 0.61, 0.32, 0.43, 0.6, 0.66, 0.41, 0.63, 0.47, 0.35, 0.78, 0.31, 0.27, 0.56, 0.74, 0.51, 0.76, 0.61, 0.32, 0.43, 0.6, 0.66, 0.41, 0.5, 0.5],
    # [0.51, 0.45, 0.48, 0.76, 0.29, 0.5, 0.49, 0.71, 0.55, 0.73, 0.57, 0.21, 0.61, 0.54, 0.63, 0.52, 0.51, 0.45, 0.48, 0.76, 0.29, 0.5, 0.49, 0.71, 0.55, 0.73, 0.57, 0.21, 0.61, 0.54, 0.63, 0.52, 0.5, 0.5],
    # [0.76, 0.62, 0.91, 0.81, 0.47, 0.79, 0.83, 0.89, 0.74, 0.79, 0.87, 0.69, 0.91, 0.84, 0.88, 0.9, 0.76, 0.62, 0.91, 0.81, 0.47, 0.79, 0.83, 0.89, 0.74, 0.79, 0.87, 0.69, 0.91, 0.84, 0.88, 0.9, 0.5, 0.5],
    # [0.78, 0.37, 0.55, 0.62, 0.21, 0.57, 0.63, 0.87, 0.64, 0.69, 0.74, 0.61, 0.74, 0.73, 0.74, 0.79, 0.78, 0.37, 0.55, 0.62, 0.21, 0.57, 0.63, 0.87, 0.64, 0.69, 0.74, 0.61, 0.74, 0.73, 0.74, 0.79, 0.5, 0.5],
    # [0.39, 0.68, 0.74, 0.78, 0.29, 0.75, 0.82, 0.86, 0.79, 0.77, 0.81, 0.57, 0.87, 0.74, 0.89, 0.81, 0.39, 0.68, 0.74, 0.78, 0.29, 0.75, 0.82, 0.86, 0.79, 0.77, 0.81, 0.57, 0.87, 0.74, 0.89, 0.81, 0.5, 0.5],
    # [0.81, 0.74, 0.81, 0.74, 0.38, 0.78, 0.76, 0.91, 0.53, 0.74, 0.8, 0.44, 0.88, 0.77, 0.82, 0.77, 0.81, 0.74, 0.81, 0.74, 0.38, 0.78, 0.76, 0.91, 0.53, 0.74, 0.8, 0.44, 0.88, 0.77, 0.82, 0.77, 0.5, 0.5],
    # [0.91, 0.71, 0.86, 0.76, 0.44, 0.89, 0.81, 0.9, 0.73, 0.79, 0.82, 0.58, 0.94, 0.8, 0.87, 0.84, 0.91, 0.71, 0.86, 0.76, 0.44, 0.89, 0.81, 0.9, 0.73, 0.79, 0.82, 0.58, 0.94, 0.8, 0.87, 0.84, 0.5, 0.5]
    # ]
    # 技术*34

    data = [
    [0.8, 0, 0, 0, 0, 0, 2, 1, 3, 4, 1, 2, 1, 3, 2, 1, 1, 1, 2, 3, 3, 2, 2, 3, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0],
    [0, 0, 0, 0, 0, 0.1, 10, 2, 2, 4, 1, 1, 1, 3, 1, 1, 1, 2, 3, 3, 3, 1, 1, 3, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0],
    [0, 0, 0.4, 0, 0, 0, 5, 3, 1, 2, 1, 4, 1, 4, 2, 2, 2, 1, 4, 3, 3, 1, 1, 3, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0]
    ]
    data = np.array(data)
    # print(data.shape)
    # 34列

    normalized_data = normalize_data(data.transpose())
    # print('normalized_data:\n', normalized_data)
    # print(normalized_data.transpose())
    A = normalized_data.transpose()
    print("A\n:", A)

    Q_1 = to_consistent_matrix(R_1)
    Q_2 = to_consistent_matrix(R_2)
    Q_3 = to_consistent_matrix(R_3)
    Q_4 = to_consistent_matrix(R_4)
    Q_5 = to_consistent_matrix(R_5)
    Q_6 = to_consistent_matrix(R_6)
    Q_7 = to_consistent_matrix(R_7)
    Q_8 = to_consistent_matrix(R_8)
    # print('Q_1, Q_2, Q_3, Q_4, Q_5, Q_6:',Q_1, Q_2, Q_3, Q_4, Q_5, Q_6)  # 将模糊判断矩阵改造为模糊一致矩阵




    W_1 = ranking_vector(Q_1)
    W_2 = ranking_vector(Q_2)
    W_3 = ranking_vector(Q_3)
    W_4 = ranking_vector(Q_4)
    W_5 = ranking_vector(Q_5)
    W_6 = ranking_vector(Q_6)
    W_7 = ranking_vector(Q_7)
    W_8 = ranking_vector(Q_8)
    # print("Ranking Vector:")
    # print(W_1, W_2, W_3, W_4, W_5, W_6)

    result_list = []
    matrices = [W_2, W_3, W_4, W_5, W_6, W_7, W_8]
    for i, matrix in enumerate(matrices):
        weighted_matrix = matrix * W_1[i]
        result_list.extend(weighted_matrix.flatten())

    # 打印最终的结果列表
    print('综合权重', result_list)


    A = np.array(A)
    alpha = 0.01
    beta = 0.01

    S_j = A.mean(axis=0) # A列均值
    print("S_j:", S_j)


    
    S_ij = calculate_deviation(A, alpha, beta)
    print("S_ij:", S_ij)

    # result_list元素*S_ij中的列
    result_list = np.array(result_list)
    result_list = result_list * S_ij
    print("result_list:", result_list)


    alpha_ij = result_list / np.sum(result_list, axis=1, keepdims=True)
    print("动态权值归一化alpha_ij:", alpha_ij)

    # row_sum = np.sum(alpha_ij, axis=1)
    # print("row_sum:", row_sum)

    # alpha_ij矩阵和A矩阵进行计算
    U_i = calculate_efficiency_values(A, alpha_ij)
    print("装备总效能值U_i:", U_i)
