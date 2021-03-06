#!/usr/bin/gosh -I.
;;
;; Copyright 2012 agwlvssainokuni
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

(use csvparser)

(define (main args) 
  (let ((err (current-error-port)))
    (guard (ex
	    ((csv-error? ex)
	     (format err "error: ~A\n" (slot-ref ex 'message)))
	    (else
	     (format err "error: ~A\n" (slot-ref ex 'message))))
	   (call-with-input-file (cadr args) main-loop))
    ))

(define (main-loop port)
  (let ((record ()))
    (until (eof-object? record)
	   (set! record (read-record port))
	   (unless (eof-object? record)
		   (display "<R>")
		   (for-each
		    (lambda (field)
		      (display "<F>")
		      (display field)
		      (display "</F>"))
		    record)
		   (display "</R>")
		   ))
    ))
