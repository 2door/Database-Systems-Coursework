# Database Systems Coursework

An interface to query a database containing entries about:
  * Students
  * Modules
  * Module History: a database of past module organizers in academic years
  * Exams: a database of exams taken by students for modules in different academic years and the students' scores
  * Module Prerequisites: a database of modules required to be taken by students in order to attend other modules

The program will show a list of options, take input numbers to select options, execute the respective query, display the result and display the options list again until the quit option (`0`) is selected.

## Options

### 1. Modules by student
Displays all the modules taken by each student, in the format: `STUDENT: MODULE1 MODULE1 ...` or `STUDENT: None` (if they take no modules).

### 2. Ghost Modules
Displays modules that have never had an exam result recorded, in the format: `MODULE1 MODULE2 ...` or `No modules to display` (if there are no ghost modules).

### 3. Popularity ratings
Displays all of the modules in order of their popularity.
Popularity is determined by the number of exams with the code of the module.
Modules with no exam entries have `null` popularity.
Modules with the same popularity (including `null`) are ordered arbitrarily.
Modules are displayed one below the other:
```
MODULE1
MODULE2
...
```
or `No modules to display`

### 4. Top Student(s)
Displays all of the students who have the highest average score.
Students with no scores (no exams) are not considered.
The students will be displayed in arbitrary order.
Student names are displayed one below the other:
```
STUDENT1
STUDENT2
...
```
or `No students to display`

### 5. Harshness ranking
Displays the harshest module organizers.
Harshness is determined by the average of all scores scored on exams held for modules delivered by an organizer.
Organizers with no exams taken for their modules are not considered.
Module organizers are displayed in arbitrary order.
Names are displayed one below the other:
```
ORGANIZER1
ORGANIZER2
```
or `No names to display`

### 6. Leaf modules
Displays all modules that have no prerequisites, in the format: `MODULE1 MODULE2` or `No modules to display` (if there are no leaf modules).

### 7. Risky exams
Displays the students who are taking exams for modules before completing all of the prerequisites for those modules, in the format: `STUDENT1 STUDENT2 ...` OR `No students to display` (if there are no students taking risky exams).

### 8. Twisted prerequisites
Displays the modules which are either part of a prerequisite cycle or have prerequisites in such a cycle, in the format: `MODULE1 MODULE2 ...` or `No modules to display` (if there are no modules with cyclical prerequisites).
