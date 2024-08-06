#!/bin/Rscript
library(argparse)
library(tidyverse)

parser <- ArgumentParser()

# specify our desired options 
# by default ArgumentParser will add an help option 
parser$add_argument(
  "-v", "--verbose", action="store_true", default=TRUE,
  help="Print extra output [default]")
parser$add_argument(
  "-q", "--quietly", action="store_false", 
  dest="verbose", help="Print little output")
parser$add_argument(
  "--csv", default="output.csv", type="character",
  help="The CSV file to process [default %(default)s]")
parser$add_argument(
  "--outputDir", default="/opt/metadata-qa/output", type="character",
  help="The CSV file to process [default %(default)s]")
parser$add_argument(
  "--fields", default="rulecatalog_score", type="character",
  help="The list of fields to process in the file [default %(default)s]")

# get command line options, if help option encountered print help and exit,
# otherwise if options not found on command line then set defaults, 
args <- parser$parse_args()

# print some progress messages to stderr if "quietly" wasn't requested
if (args$verbose) { 
  write("writing some verbose output to standard error...\n", stderr()) 
}

print(paste("csv:", args$csv))
print(paste("fields:", args$fields))
fields <- unlist(strsplit(args$fields, split = ","))

df <- read_csv(args$csv)
total <- nrow(df)

make_stat <- function(field) {
  # print(paste("field: ", field))
  freq <- df %>% select(all_of(field)) %>% table() %>% as_tibble()
  names(freq) <- c('value', 'count')
  freq$percent <- freq$count * 100 / total
  write_csv(freq, paste0(args$outputDir, "/", field, ".csv"))
}

lapply(fields, make_stat)

cat("\n")