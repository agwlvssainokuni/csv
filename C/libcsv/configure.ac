#                                               -*- Autoconf -*-
# Process this file with autoconf to produce a configure script.

AC_PREREQ([2.68])
AC_INIT([libcsv], [0.0])
AC_CONFIG_SRCDIR([config.h.in])
AC_CONFIG_HEADERS([config.h])
AC_CONFIG_MACRO_DIR([m4])
AM_INIT_AUTOMAKE

# Checks for programs.
AC_PROG_CXX
AC_PROG_AWK
AC_PROG_CC
AC_PROG_CPP
AC_PROG_INSTALL
AC_PROG_LN_S
AC_PROG_MAKE_SET
AC_PROG_RANLIB

LT_INIT

# Checks for libraries.

# Checks for header files.
AC_CHECK_HEADERS([stdlib.h])

# Checks for typedefs, structures, and compiler characteristics.

# Checks for library functions.


AC_ARG_ENABLE(parser,
[  --disable-parser  Do not build csvparser],
[case "${enableval}" in
  yes) parser=true  ;;
  no)  parser=false ;;
  *)   AC_MSG_ERROR(bad value ${enableval} for --disable-parser) ;;
esac], [parser=true])
AM_CONDITIONAL(PARSER, test x$parser = xtrue)


AC_CONFIG_FILES([Makefile
                 src/Makefile
                 src/parser/Makefile
                 src/parser/sample/Makefile
                 src/state/Makefile
                 src/state/sample/Makefile])
AC_OUTPUT
