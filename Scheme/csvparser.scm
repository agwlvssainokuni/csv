;;
;; Copyright 2012 Norio Agawa
;;
;; Licensed under the Apache License, Version 2.0 (the "License");
;; you may not use this file except in compliance with the License.
;; You may obtain a copy of the License at
;;
;;     http://www.apache.org/licenses/LICENSE-2.0
;;
;; Unless required by applicable law or agreed to in writing, software
;; distributed under the License is distributed on an "AS IS" BASIS,
;; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
;; See the License for the specific language governing permissions and
;; limitations under the License.
;;

(define-module csvparser
  (export read-record <csv-error>))
(select-module csvparser)

(define-class <csv-error> (<error>) ())

(define (read-record port)
  (let
      ((record #f)
       (field (open-output-string))
       (state RECORD_BEGIN))
    (until (eq? state RECORD_END)
	   (let*
	       ((pch (read-char port))
		(ch (if (eof-object? pch) 'EOF pch))
		(trans (state ch)))
	     (case (car trans)
	       ((APPEND)
		(write-char ch field))
	       ((FLUSH)
		(unless record (set! record ()))
		(set! record
		      (append record (list (get-output-string field))))
		(close-output-port field)
		(set! field (open-output-string)))
	       ((ERROR)
		(raise (condition
			(<csv-error> (message "Invalid CSV format"))))))
	     (set! state (cadr trans))
	     ))
    (close-output-port field)
    record
    ))

(define (RECORD_BEGIN ch)
  (case ch
    ((#\,) `(FLUSH ,FIELD_BEGIN))
    ((#\") `(NONE ,ESCAPED))
    ((#\CR) `(FLUSH ,CR))
    ((#\LF) `(FLUSH ,RECORD_END))
    ((EOF) `(NONE ,RECORD_END))
    (else `(APPEND ,NONESCAPED))
    ))

(define (FIELD_BEGIN ch)
  (case ch
    ((#\,) `(FLUSH ,FIELD_BEGIN))
    ((#\") `(NONE ,ESCAPED))
    ((#\CR) `(FLUSH ,CR))
    ((#\LF) `(FLUSH ,RECORD_END))
    ((EOF) `(FLUSH ,RECORD_END))
    (else `(APPEND ,NONESCAPED))
    ))

(define (NONESCAPED ch)
  (case ch
    ((#\,) `(FLUSH ,FIELD_BEGIN))
    ((#\") `(APPEND ,NONESCAPED))
    ((#\CR) `(FLUSH ,CR))
    ((#\LF) `(FLUSH ,RECORD_END))
    ((EOF) `(FLUSH ,RECORD_END))
    (else `(APPEND ,NONESCAPED))
    ))

(define (ESCAPED ch)
  (case ch
    ((#\,) `(APPEND ,ESCAPED))
    ((#\") `(NONE ,DQUOTE))
    ((#\CR) `(APPEND ,ESCAPED))
    ((#\LF) `(APPEND ,ESCAPED))
    ((EOF) `(ERROR ()))
    (else `(APPEND ,ESCAPED))
    ))

(define (DQUOTE ch)
  (case ch
    ((#\,) `(FLUSH ,FIELD_BEGIN))
    ((#\") `(APPEND ,ESCAPED))
    ((#\CR) `(FLUSH ,CR))
    ((#\LF) `(FLUSH ,RECORD_END))
    ((EOF) `(FLUSH ,RECORD_END))
    (else `(ERROR ()))
    ))

(define (CR ch)
  (case ch
    ((#\,) `(ERROR ()))
    ((#\") `(ERROR ()))
    ((#\CR) `(NONE ,CR))
    ((#\LF) `(NONE ,RECORD_END))
    ((EOF) `(NONE ,RECORD_END))
    (else `(ERROR ()))
    ))

(define (RECORD_END ch) ())
