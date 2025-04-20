import java.text.DecimalFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Main {

    // 矩阵转置
    private static double[][] transpose(double[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;
        double[][] result = new double[cols][rows];
        for (int i = 0; i < cols; i++) {
            for (int j = 0; j < rows; j++) {
                result[i][j] = matrix[j][i];
            }
        }
        return result;
    }

    private static double[][] normalizeData(double[][] data, int[] posInd, int[] negInd) {
        int rows = data.length;
        int cols = data[0].length;
        double[][] normalized = new double[rows][cols];

        // 处理正指标
        for (int i : posInd) {
            if (i >= rows) continue;
            double[] col = data[i];
            double min = Arrays.stream(col).min().getAsDouble();
            double max = Arrays.stream(col).max().getAsDouble();
            for (int j = 0; j < cols; j++)
                normalized[i][j] = (max != min) ? (col[j]-min)/(max-min) : 1;
        }

        // 处理负指标
        for (int i : negInd) {
            if (i >= rows) continue;
            double[] col = data[i];
            double min = Arrays.stream(col).min().getAsDouble();
            double max = Arrays.stream(col).max().getAsDouble();
            for (int j = 0; j < cols; j++)
                normalized[i][j] = (max != min) ? (max - col[j])/(max-min) : 1;
        }
        return normalized;
    }

    // 创建全值矩阵
    private static double[][] createFullMatrix(int size, double value) {
        double[][] matrix = new double[size][size];
        for (double[] row : matrix) {
            Arrays.fill(row, value);
        }
        return matrix;
    }

    // 转换为一致矩阵
    private static double[][] toConsistentMatrix(double[][] matrix) {
        int n = matrix.length;
        double[] xi = new double[n];
        for (int i = 0; i < n; i++) {
            xi[i] = sum(matrix[i]);
        }

        double[][] Q = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                Q[i][j] = (xi[i] - xi[j]) / (2 * n) + 0.5;
            }
        }
        return Q;
    }

    // 计算排序向量
    private static double[] rankingVector(double[][] F) {
        int n = F.length;
        double a = (n-1)/2.0;
        double[] w = new double[n];
        for (int i = 0; i < n; i++) {
            double sum = sum(F[i]);
            w[i] = 1.0/n - 1.0/(2*a) + sum/(n*a);
        }
        return w;
    }

    // 计算偏离度矩阵
    private static double[][] calculateDeviation(double[][] A, double alpha, double beta) {
        int rows = A.length;
        int cols = A[0].length;
        double[] s_j = new double[cols];
        Arrays.fill(s_j, 0.0);

        // 计算列均值
        for (int j = 0; j < cols; j++) {
            for (int i = 0; i < rows; i++) {
                s_j[j] += A[i][j];
            }
            s_j[j] /= rows;
        }

        // 计算偏离度
        double[][] S = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (A[i][j] <= s_j[j]) {
                    S[i][j] = (alpha + s_j[j]) / (alpha + A[i][j]);
                } else {
                    S[i][j] = (beta + A[i][j]) / (beta + s_j[j]);
                }
            }
        }
        return S;
    }

    // 行归一化
    private static double[][] normalizeRows(double[][] matrix) {
        double[][] result = new double[matrix.length][matrix[0].length];
        for (int i = 0; i < matrix.length; i++) {
            double sum = sum(matrix[i]);
            for (int j = 0; j < matrix[i].length; j++) {
                result[i][j] = matrix[i][j] / sum;
            }
        }
        return result;
    }

    // 计算效能值
    private static double[] calculateEfficiencyValues(double[][] A, double[][] alpha) {
        double[] results = new double[A.length];
        for (int i = 0; i < A.length; i++) {
            double sum = 0;
            for (int j = 0; j < A[i].length; j++) {
                sum += alpha[i][j] * A[i][j];
            }
            results[i] = sum;
        }
        return results;
    }

    // 辅助函数：数组求和
    private static double sum(double[] arr) {
        double sum = 0;
        for (double num : arr) {
            sum += num;
        }
        return sum;
    }

    // 打印矩阵
    private static void printMatrix(double[][] matrix) {
        for (double[] row : matrix) {
            System.out.println(Arrays.toString(row));
        }
    }

    // 格式化输出
    private static void printRankedResults(double[] values) {
        // 创建索引-数值对
        List<Map.Entry<Integer, Double>> entries = new ArrayList<>();
        for (int i = 0; i < values.length; i++) {
            entries.add(new AbstractMap.SimpleEntry<>(i + 1, values[i])); // 假设装备编号从1开始
        }

        // 降序排序
        Collections.sort(entries, (a, b) -> Double.compare(b.getValue(), a.getValue()));

        // 格式化输出
        System.out.println("\n=== 装备效能排行榜 ===");
        DecimalFormat df = new DecimalFormat("0.0000");

        for (int i = 0; i < entries.size(); i++) {
            Map.Entry<Integer, Double> entry = entries.get(i);
            System.out.printf("第%d名: 装备%-2d | 效能值: %s%n",
                    i + 1,
                    entry.getKey(),
                    df.format(entry.getValue()));
        }
    }

    // 添加一个新的GUI界面来修改矩阵
    private static class MatrixEditor extends JFrame {
        private HashMap<String, double[][]> matrices = new HashMap<>();
        private JComboBox<String> matrixSelector;
        private JTable matrixTable;
        private DefaultTableModel tableModel;
        private JSpinner rowSpinner, colSpinner;
        private JButton applyDimensionButton, calculateButton;
        private int numTechnologies = 3;
        private JTextArea resultsArea;
        private Font largeFont = new Font("Dialog", Font.PLAIN, 20);

        public MatrixEditor() {
            super("效能评估");
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setSize(1200, 800);
            setLayout(new BorderLayout(10, 10));

            // 应用全局字体
            setUIFont(largeFont);

            // 初始化矩阵
            matrices.put("data (技术评价数据)", new double[3][34]);
            matrices.put("R1 (一级指标判断矩阵)", createFullMatrix(7, 0.5));
            matrices.put("R2 (二级指标判断矩阵1)", createFullMatrix(6, 0.5));
            matrices.put("R3 (二级指标判断矩阵2)", createFullMatrix(3, 0.5));
            matrices.put("R4 (二级指标判断矩阵3)", createFullMatrix(3, 0.5));
            matrices.put("R5 (二级指标判断矩阵4)", createFullMatrix(7, 0.5));
            matrices.put("R6 (二级指标判断矩阵5)", createFullMatrix(8, 0.5));
            matrices.put("R7 (二级指标判断矩阵6)", createFullMatrix(4, 0.5));
            matrices.put("R8 (二级指标判断矩阵7)", createFullMatrix(3, 0.5));

            // 设置示例数据
            double[][] data = {
                    {0.8,0,0,0,0,0,2,1,3,4,1,2,1,3,2,1,1,1,2,3,3,2,2,3,0,0,0,0,0,0,0,0,0,0},
                    {0,0,0,0,0,0.1,10,2,2,4,1,1,1,3,1,1,1,2,3,3,3,1,1,3,0,0,0,0,0,0,0,0,0,0},
                    {0,0,0.4,0,0,0,5,3,1,2,1,4,1,4,2,2,2,1,4,3,3,1,1,3,0,0,0,0,0,0,0,0,0,0}
            };
            matrices.put("data (技术评价数据)", data);

            // 设置R1示例数据
            double[][] R1 = {
                    {0.5,0.6,0.7,0.7,0.6,0.6,0.9},
                    {0.4,0.5,0.8,0.7,0.6,0.6,0.9},
                    {0.3,0.2,0.5,0.5,0.5,0.5,0.9},
                    {0.3,0.3,0.5,0.5,0.5,0.5,0.5},
                    {0.4,0.4,0.5,0.5,0.5,0.5,0.5},
                    {0.4,0.4,0.5,0.5,0.5,0.5,0.5},
                    {0.1,0.1,0.1,0.5,0.5,0.5,0.5}
            };
            matrices.put("R1 (一级指标判断矩阵)", R1);

            // 设置R2示例数据
            double[][] R2 = {
                    {0.5,0.55,0.6,0.7,0.8,0.9},
                    {0.45,0.5,0.55,0.6,0.7,0.8},
                    {0.4,0.45,0.5,0.55,0.6,0.7},
                    {0.3,0.4,0.45,0.5,0.55,0.6},
                    {0.2,0.3,0.4,0.45,0.5,0.55},
                    {0.1,0.2,0.3,0.4,0.45,0.5}
            };
            matrices.put("R2 (二级指标判断矩阵1)", R2);

            // 设置R3示例数据
            double[][] R3 = {
                    {0.5,0.2,0.1},
                    {0.8,0.5,0.3},
                    {0.9,0.7,0.5}
            };
            matrices.put("R3 (二级指标判断矩阵2)", R3);

            // 创建顶部控制面板
            JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
            controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            JLabel selectLabel = new JLabel("选择矩阵:");
            selectLabel.setFont(largeFont);

            matrixSelector = new JComboBox<>(matrices.keySet().toArray(new String[0]));
            matrixSelector.setFont(largeFont);
            matrixSelector.addActionListener(e -> updateTableForMatrix((String) matrixSelector.getSelectedItem()));

            JLabel rowLabel = new JLabel("行数:");
            rowLabel.setFont(largeFont);

            rowSpinner = new JSpinner(new SpinnerNumberModel(3, 1, 50, 1));
            rowSpinner.setFont(largeFont);
            JComponent fieldEditor = rowSpinner.getEditor();
            if (fieldEditor instanceof JSpinner.DefaultEditor) {
                ((JSpinner.DefaultEditor)fieldEditor).getTextField().setFont(largeFont);
            }

            JLabel colLabel = new JLabel("列数:");
            colLabel.setFont(largeFont);

            colSpinner = new JSpinner(new SpinnerNumberModel(34, 1, 50, 1));
            colSpinner.setFont(largeFont);
            fieldEditor = colSpinner.getEditor();
            if (fieldEditor instanceof JSpinner.DefaultEditor) {
                ((JSpinner.DefaultEditor)fieldEditor).getTextField().setFont(largeFont);
            }

            applyDimensionButton = new JButton("设置待评价技术数量");
            applyDimensionButton.setFont(largeFont);
            applyDimensionButton.addActionListener(e -> resizeMatrix());

            JButton saveButton = new JButton("保存矩阵");
            saveButton.setFont(largeFont);
            saveButton.addActionListener(e -> saveCurrentMatrix());

            calculateButton = new JButton("计算最终效能值");
            calculateButton.setFont(largeFont);
            calculateButton.addActionListener(e -> calculateEfficiency());

            controlPanel.add(selectLabel);
            controlPanel.add(matrixSelector);
            controlPanel.add(rowLabel);
            controlPanel.add(rowSpinner);
            controlPanel.add(colLabel);
            controlPanel.add(colSpinner);
            controlPanel.add(applyDimensionButton);
            controlPanel.add(saveButton);
            controlPanel.add(calculateButton);
            
            // 创建表格
            tableModel = new DefaultTableModel();
            matrixTable = new JTable(tableModel);
            matrixTable.setFont(largeFont);
            matrixTable.setRowHeight(30);
            JTableHeader header = matrixTable.getTableHeader();
            header.setFont(largeFont);

            JScrollPane scrollPane = new JScrollPane(matrixTable);

            // 创建结果显示区域
            resultsArea = new JTextArea(10, 50);
            resultsArea.setFont(largeFont);
            resultsArea.setEditable(false);
            JScrollPane resultScrollPane = new JScrollPane(resultsArea);

            // 使用分割面板
            JSplitPane splitPane = new JSplitPane(
                    JSplitPane.VERTICAL_SPLIT,
                    scrollPane,
                    resultScrollPane
            );
            splitPane.setDividerLocation(400);

            add(controlPanel, BorderLayout.NORTH);
            add(splitPane, BorderLayout.CENTER);

            // 初始加载第一个矩阵
            updateTableForMatrix((String) matrixSelector.getSelectedItem());

            setLocationRelativeTo(null);
            setVisible(true);
        }

        // 设置UI字体
        private static void setUIFont(Font font) {
            UIManager.put("Button.font", font);
            UIManager.put("Label.font", font);
            UIManager.put("ComboBox.font", font);
            UIManager.put("TextField.font", font);
            UIManager.put("TextArea.font", font);
            UIManager.put("Table.font", font);
            UIManager.put("TableHeader.font", font);
        }

        private void updateTableForMatrix(String matrixName) {
            double[][] matrix = matrices.get(matrixName);
            int rows = matrix.length;
            int cols = matrix[0].length;

            // 更新行列输入框
            rowSpinner.setValue(rows);
            colSpinner.setValue(cols);

            // 更新表格模型
            tableModel.setRowCount(0);
            tableModel.setColumnCount(0);

            // 添加列标题
            for (int i = 0; i < cols; i++) {
                tableModel.addColumn("指标 " + (i+1));
            }

            // 添加行数据
            for (int i = 0; i < rows; i++) {
                Object[] rowData = new Object[cols];
                for (int j = 0; j < cols; j++) {
                    rowData[j] = matrix[i][j];
                }
                tableModel.addRow(rowData);
            }

            // 如果是data矩阵，启用行列调整
            boolean isDataMatrix = matrixName.startsWith("data");
            rowSpinner.setEnabled(isDataMatrix);
            colSpinner.setEnabled(false); // 列数固定为34
            applyDimensionButton.setEnabled(isDataMatrix);

            // 如果是判断矩阵，确保是方阵
            if (!isDataMatrix) {
                colSpinner.setValue(rows);
            }
        }

        private void resizeMatrix() {
            String matrixName = (String) matrixSelector.getSelectedItem();
            int newRows = (int) rowSpinner.getValue();
            int newCols = (int) colSpinner.getValue();

            // 如果是判断矩阵，确保是方阵
            if (!matrixName.startsWith("data")) {
                newCols = newRows;
                colSpinner.setValue(newRows);
            }

            // 创建新矩阵，保留原有数据
            double[][] oldMatrix = matrices.get(matrixName);
            double[][] newMatrix = new double[newRows][newCols];

            // 复制数据
            for (int i = 0; i < Math.min(oldMatrix.length, newRows); i++) {
                for (int j = 0; j < Math.min(oldMatrix[0].length, newCols); j++) {
                    newMatrix[i][j] = oldMatrix[i][j];
                }
            }

            // 更新矩阵
            matrices.put(matrixName, newMatrix);

            // 如果是data矩阵，更新技术数量
            if (matrixName.startsWith("data")) {
                numTechnologies = newRows;
            }

            // 刷新表格
            updateTableForMatrix(matrixName);
        }

        private void saveCurrentMatrix() {
            // 保存当前显示的矩阵数据
            String currentMatrix = (String) matrixSelector.getSelectedItem();
            double[][] matrix = matrices.get(currentMatrix);

            for (int i = 0; i < matrix.length; i++) {
                for (int j = 0; j < matrix[0].length; j++) {
                    try {
                        matrix[i][j] = Double.parseDouble(matrixTable.getValueAt(i, j).toString());
                    } catch (Exception e) {
                        matrix[i][j] = 0;
                    }
                }
            }

            resultsArea.setText("矩阵 " + currentMatrix + " 已保存！\n");
        }

        private void calculateEfficiency() {
            // 先保存当前矩阵
            saveCurrentMatrix();

            // 从矩阵集合中获取数据
            double[][] data = matrices.get("data (技术评价数据)");
            double[][] R1 = matrices.get("R1 (一级指标判断矩阵)");
            double[][] R2 = matrices.get("R2 (二级指标判断矩阵1)");
            double[][] R3 = matrices.get("R3 (二级指标判断矩阵2)");
            double[][] R4 = matrices.get("R4 (二级指标判断矩阵3)");
            double[][] R5 = matrices.get("R5 (二级指标判断矩阵4)");
            double[][] R6 = matrices.get("R6 (二级指标判断矩阵5)");
            double[][] R7 = matrices.get("R7 (二级指标判断矩阵6)");
            double[][] R8 = matrices.get("R8 (二级指标判断矩阵7)");

            // 在计算前显示所有矩阵的当前值
            StringBuilder matrixInfo = new StringBuilder();

            // 添加时间戳
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String timestamp = dateFormat.format(new Date());
            matrixInfo.append("=== 计算记录 [").append(timestamp).append("] ===\n\n");

            // 显示data矩阵
            matrixInfo.append("【技术评价数据矩阵】 (").append(data.length).append("×").append(data[0].length).append(")\n");
            for (int i = 0; i < data.length; i++) {
                matrixInfo.append("技术").append(i + 1).append(": ");
                for (int j = 0; j < Math.min(data[i].length, 100); j++) { // 只显示前10个元素，避免过长
                    matrixInfo.append(String.format("%.2f", data[i][j])).append(" ");
                }
                if (data[i].length > 100) matrixInfo.append("...");
                matrixInfo.append("\n");
            }
            matrixInfo.append("\n");

            // 显示各判断矩阵
            appendMatrixInfo(matrixInfo, "R1 (一级指标判断矩阵)", R1);
            appendMatrixInfo(matrixInfo, "R2 (二级指标判断矩阵1)", R2);
            appendMatrixInfo(matrixInfo, "R3 (二级指标判断矩阵2)", R3);
            appendMatrixInfo(matrixInfo, "R4 (二级指标判断矩阵3)", R4);
            appendMatrixInfo(matrixInfo, "R5 (二级指标判断矩阵4)", R5);
            appendMatrixInfo(matrixInfo, "R6 (二级指标判断矩阵5)", R6);
            appendMatrixInfo(matrixInfo, "R7 (二级指标判断矩阵6)", R7);
            appendMatrixInfo(matrixInfo, "R8 (二级指标判断矩阵7)", R8);

            matrixInfo.append("\n开始计算效能值...\n\n");
            resultsArea.setText(matrixInfo.toString());

            // 创建结果字符串构建器
            StringBuilder resultText = new StringBuilder();

            // 进行计算过程，和原始main方法一致
            try {
                // 转置数据后进行归一化
                int[] posIndicators = {0,1,2,3,4,5,8,19,20,23,26,27,28,29,30,31,32,33};
                int[] negIndicators = {6,7,9,10,11,12,13,14,15,16,17,18,21,22,24,25};
                double[][] dataTransposed = transpose(data);
                double[][] normalizedData = normalizeData(dataTransposed, posIndicators, negIndicators);
                double[][] A = transpose(normalizedData);

                // 转换为一致矩阵
                double[][] Q1 = toConsistentMatrix(R1);
                double[][] Q2 = toConsistentMatrix(R2);
                double[][] Q3 = toConsistentMatrix(R3);
                double[][] Q4 = toConsistentMatrix(R4);
                double[][] Q5 = toConsistentMatrix(R5);
                double[][] Q6 = toConsistentMatrix(R6);
                double[][] Q7 = toConsistentMatrix(R7);
                double[][] Q8 = toConsistentMatrix(R8);

                // 计算排序向量
                double[] W1 = rankingVector(Q1);
                double[] W2 = rankingVector(Q2);
                double[] W3 = rankingVector(Q3);
                double[] W4 = rankingVector(Q4);
                double[] W5 = rankingVector(Q5);
                double[] W6 = rankingVector(Q6);
                double[] W7 = rankingVector(Q7);
                double[] W8 = rankingVector(Q8);

                // 组合综合权重
                List<Double> resultList = new ArrayList<>();
                double[][] matrices = {W2, W3, W4, W5, W6, W7, W8};
                for (int i = 0; i < matrices.length; i++) {
                    double weight = W1[i];
                    for (double value : matrices[i]) {
                        resultList.add(value * weight);
                    }
                }
                double[] resultArray = new double[resultList.size()];
                for (int i = 0; i < resultArray.length; i++) {
                    resultArray[i] = resultList.get(i);
                }

                // 计算偏离度矩阵
                double alpha = 0.01;
                double beta = 0.01;
                double[][] Sij = calculateDeviation(A, alpha, beta);

                // 扩展权重矩阵并进行元素相乘
                double[][] resultMatrix = new double[data.length][34];
                for (int i = 0; i < data.length; i++) {
                    System.arraycopy(resultArray, 0, resultMatrix[i], 0, 34);
                }
                double[][] multiplied = new double[data.length][34];
                for (int i = 0; i < data.length; i++) {
                    for (int j = 0; j < 34; j++) {
                        multiplied[i][j] = resultMatrix[i][j] * Sij[i][j];
                    }
                }

                // 归一化动态权值矩阵
                double[][] alphaIj = normalizeRows(multiplied);

                // 计算最终效能值
                double[] Ui = calculateEfficiencyValues(A, alphaIj);

                // 在结果区域显示结果
                resultText.append("=== 装备效能排行榜 ===\n\n");

                // 创建索引-数值对
                List<Map.Entry<Integer, Double>> entries = new ArrayList<>();
                for (int i = 0; i < Ui.length; i++) {
                    entries.add(new AbstractMap.SimpleEntry<>(i + 1, Ui[i]));
                }

                // 降序排序
                Collections.sort(entries, (a, b) -> Double.compare(b.getValue(), a.getValue()));

                // 格式化输出
                DecimalFormat df = new DecimalFormat("0.0000");
                for (int i = 0; i < entries.size(); i++) {
                    Map.Entry<Integer, Double> entry = entries.get(i);
                    String rankLine = String.format("第%d名: 装备%-2d | 效能值: %s\n",
                            i + 1,
                            entry.getKey(),
                            df.format(entry.getValue()));
                    resultText.append(rankLine);
                }

                // 将结果追加到结果区域
                resultsArea.setText(matrixInfo.toString() + resultText.toString());

                // 将所有信息写入到文件
                String logContent = matrixInfo.toString() + resultText.toString() +
                        "\n" + "=".repeat(50) + "\n\n";
                writeToLogFile(logContent);

            } catch (Exception e) {
                String errorMsg = "\n计算过程中出错：" + e.getMessage() + "\n请检查矩阵数据是否正确。";
                resultsArea.setText(matrixInfo + errorMsg);

                // 将错误信息也写入日志
                String logContent = matrixInfo.toString() + errorMsg +
                        "\n" + "=".repeat(50) + "\n\n";
                writeToLogFile(logContent);

                e.printStackTrace();
            }
        }

        // 辅助方法：将矩阵信息添加到StringBuilder
        private void appendMatrixInfo(StringBuilder sb, String matrixName, double[][] matrix) {
            sb.append("【").append(matrixName).append("】 (")
                    .append(matrix.length).append("×").append(matrix[0].length).append(")\n");

            DecimalFormat df = new DecimalFormat("0.00");
            for (int i = 0; i < matrix.length; i++) {
                for (int j = 0; j < matrix[i].length; j++) {
                    sb.append(df.format(matrix[i][j])).append(" ");
                }
                sb.append("\n");
            }
            sb.append("\n");
        }

        // 将内容写入日志文件
        private void writeToLogFile(String content) {
            try {
                // 创建日志文件目录（如果不存在）
                File dir = new File("logs");
                if (!dir.exists()) {
                    dir.mkdir();
                }

                // 创建日志文件名（日期格式）
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String fileName = "logs/效能评估记录_" + dateFormat.format(new Date()) + ".txt";

                // 以追加模式写入文件
                FileWriter fw = new FileWriter(fileName, true);
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(content);
                bw.close();

                // 在结果区域显示保存成功信息
                resultsArea.append("\n计算记录已保存到文件: " + fileName);

            } catch (IOException e) {
                resultsArea.append("\n保存记录文件时出错: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        // 启动GUI界面
        SwingUtilities.invokeLater(() -> new MatrixEditor());
    }
}

