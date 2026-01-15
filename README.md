# Optimization Algorithms – Packing Problem

This repository contains a Java-based implementation of several optimization algorithms for solving a **2D rectangle packing problem**. The project was developed as part of the course **"Optimization Algorithms"** at **TU Darmstadt**.

The goal of the problem is to place a set of randomly generated rectangles into fixed-size boxes such that the packing quality is optimized (minimizing the number of used boxes).

---

## Implemented Algorithms

### Local Search (Metropolis)

The Local Search approach explores the solution space using the Metropolis criterion. Three different neighborhood structures are implemented:

* **Geometry-based neighborhood**
* **Permutation-based neighborhood**
* **Partial-overlap neighborhood**

### Greedy Algorithm

Two greedy heuristics based on different sorting strategies are provided:

* **Max-area-first**
* **Max-side-first**

For a detailed problem description and constraints, please refer to the exercise sheet available in this repository:

* [German](./exercise-sheet/OptAlgos_Programmieraufgabe_2025-26.pdf) and [English](./exercise-sheet/OptAlgos_Assignment_English_Full.pdf) versions (English translated using ChatGPT).

---

## Project Structure

The project is implemented in **Java** and uses **Maven** as the build tool.

```
├── src
    ├── main
        ├── java
            ├── org.example
                ├── model
                    ├── core         # Abstract interfaces for search algorithms
                    ├── problem      # Packing problem implementation
                ├── controller       # Main application logic
                ├── Main.java        # Main entry point
        ├── resources
            ├── gui                  # GUI resources (JavaFX)
    ├── test
        ├── java                     # Unit tests for algorithms
        ├── resources                # Test input files
├── testResults                      # JSON output from test runs
├── excelResults                     # Excel files generated from results
├── post-process-results.py          # Python script for result analysis
├── pom.xml                          # Maven configuration
```

---

## Requirements

* **Java** (recommended: Java 25)
* **Maven**
* Python 3 (only required for post-processing test results)

JavaFX dependencies are managed automatically via Maven.

---

## Quick Start

```bash
git clone https://github.com/bpminh1/optimization-algorithm
cd optimization-algorithm
mvn clean install
mvn javafx:run
```

---

## Running the Application
The main application can be started using:

```bash
mvn javafx:run
```

This will launch a GUI where you can configure and run the packing algorithms.

---

## Running Tests

Three test cases are provided in:

```
src/test/resources
```

To switch between test cases, modify the following constants in:

```
src/test/java/AlgorithmTest.java
```

* `TEST_CONFIG_FILE`
* `OUTPUT_FOLDER`

Run all tests using:

```bash
mvn test
```

Test results are stored in JSON format in the `testResults` directory.

---

## Post-Processing Test Results

A Python script is provided to analyze the test outputs and generate Excel files for easier comparison and visualization.

```bash
python post-process-results.py
```

The generated Excel files will be saved in the `excelResults` directory.

To switch to the desired test case, modify the following constants in:

```
post-process-results.py
```
* `OUTPUT_FOLDER`
* `EXCEL_FOLDER`

---

## Notes

* This project was developed for educational purposes.
* The focus is on comparing heuristic strategies rather than achieving globally optimal solutions.
* The code is structured to make it easy to add new neighborhood structures or greedy strategies and solve different problems.

