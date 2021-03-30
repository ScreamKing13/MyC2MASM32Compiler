.586
.model flat, stdcall
option casemap : none
include C:\masm32\include\windows.inc
include C:\masm32\include\kernel32.inc
include C:\masm32\include\user32.inc
include module.inc
include longop.inc
includelib C:\masm32\lib\kernel32.lib
includelib C:\masm32\lib\user32.lib
.const
var0 DWORD 1.1
var1 DWORD 3.3
var2 DWORD 2.2
var3 DWORD 1.1
var4 DWORD 4.4
.data
_a DWORD ?
_c DWORD 3 dup(?)
_d DWORD ?
_b DWORD ?
_addr_ DWORD ?
_temp_ DWORD ?
.code
main:
fld var0
fstp _a


fld var1
fstp [_c + 4 * 0]


fld var2
fstp [_c + 4 * 1]


fld var3
fstp [_c + 4 * 2]


fld var4
fstp _d


invoke ExitProcess, 0
end main
