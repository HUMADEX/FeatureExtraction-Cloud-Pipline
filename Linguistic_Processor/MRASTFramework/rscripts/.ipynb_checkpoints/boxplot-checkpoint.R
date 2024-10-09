library(dplyr)
library(ggplot2)

pastwords_depressed <- [0.0221, 0.0484, 0.0112, 0.0631, 0.0988, 0.0354, 0.0208, 0.1429, 0.0686, 0.0761, 0.0358, 0.0667, 0.0324, 0.0402]
pastwords_nondepressed <- [0.0577, 0.0652, 0.0831, 0.0344, 0.0947, 0.0566, 0.0556, 0.0484, 0.0492, 0.1138, 0.0456, 0.0208, 0.0239, 0.0308]

# gererate normal distribution with same mean and sd
pwd_norm <- rnorm(200,mean=mean(pastwords_depressed, na.rm=TRUE), sd=sd(pastwords_depressed, na.rm=TRUE))
pwnd_norm <- rnorm(200,mean=mean(pastwords_nondepressed, na.rm=TRUE), sd=sd(pastwords_nondepressed, na.rm=TRUE))


boxplot(pastwords_depressed , pwd_norm, pastwords_nondepressed, pwnd_norm,
main = "Multiple boxplots for comparision",
at = c(1,2,4,5),
names = c("D", "normal", "ND", "normal"),
las = 2,
col = c("orange","red"),
border = "brown",
horizontal = TRUE,
notch = TRUE
)