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

# print(paste("csv:", args$csv))
# print(paste("fields:", args$fields))
fields <- unlist(strsplit(args$fields, split = ","))

df <- read_csv(args$csv, show_col_types = FALSE)
total <- nrow(df)
count_df <- tibble(total = total)
# print(count_df)
write_csv(count_df, paste0(args$outputDir, "/count.csv"))

# print('make status')
status <- df %>% select(id, ends_with('_status'))
score <- df %>% select(id, ends_with('_score'))

make_stat <- function(field) {
  print(paste("field: ", field))
  freq <- score %>% select(all_of(field)) %>% table() %>% as_tibble()
  names(freq) <- c('value', 'count')
  freq$percent <- freq$count * 100 / total
  write_csv(freq, paste0(args$outputDir, "/", field, ".csv"))
}

# print('create field list')
all_fields    <- names(df)
score_fields  <- all_fields[grep('_score$', all_fields)]
score_fields
status_fields <- all_fields[grep('_status$', all_fields)]
status_fields

# print('calculate score')
lapply(score_fields, make_stat)

# print('calculate status')
df_stat <- tibble(
  'id' = character(),
  '0' = numeric(),
  '1' = numeric(),
  'NA' = numeric()
)
keys <- names(df_stat)
for (row in 1:length(status_fields)) {
  column <- status_fields[row]
  df_stat[row, 1] <- column

  values <- status %>% select(all_of(column))
  t <- table(values, useNA = "always")
  names <- names(t)
  names[is.na(names)] <- 'NA'
  values <- as.numeric(t)
  for (i in 2:4) {
    k <- keys[i]
    if (sum(names == k) == 0) {
      v <- 0
    } else {
      v <- values[names == k]
    }
    df_stat[row, i] <- v
  }
}
df_stat

write_csv(df_stat, paste0(args$outputDir, '/shacl4bib-stat.csv'))
cat("\n")