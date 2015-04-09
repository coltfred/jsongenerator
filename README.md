Json File Generator
======================

## What is this?

This is used to generate random heterogenous json records such that properties can be dependent
on the previously generated values.

## Running main

For now we'll just use SBT to run the project. Start up SBT in the root
directory of the project and use the following commands:

    run-main coltfred.jsongenerator.Main <input file> <output file> <record count>

Input file is described below in the input format section. Output file 
is where the result will be written. Record count is how many random records 
will be written to that output file.

## Input Format

The input format is a single root variable with any number of choices. 
Each of those choices can have any number of dependent variables of their own.
All choice probabilities must add up to 1.0. There is a sample file included
in the base directory of this repository. It's named `sample-input.json`.

    {
      "label": "variable label",
      "choices": [
        {
          "value": "variable value",
          "probability": 0.3,
          "dependents": [
            {
              "label": "nested variable label",
              "choices": [
                {
                  "value": "nested variable value",
                  "probability": 0.7,
                  "dependents": [...]
                }
                ...
              ]
            }
          ]
        }
      ]
    }

## Output format

The output will be a single json array containing json objects that have the format:

    {
      "variable label": "variable value",
      "variable label2": "variable value2",
      ... 
    }

I've included a sample output, which is in the root and is name `sample-output.json`.
