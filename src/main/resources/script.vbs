''Identify those files in %windir%\Installer which CAN'T be removed

' Enumerate all products
Function FindRegisteredPatches()
	Dim output : output = "###begin###" & vbCrLf
	Dim msi : Set msi = CreateObject("WindowsInstaller.Installer")
	Dim products : Set products = msi.Products
	Dim productCode

	For Each productCode in products
		' For each product, enumerate its applied patches
		Dim patches : Set patches = msi.Patches(productCode)
		Dim patchCode

		For Each patchCode in patches
			' Get the local patch location
			Dim location : location = msi.PatchInfo(patchCode, "LocalPackage")
			'objFile.WriteLine productCode & ", " & patchCode & ", " & location
			'output = output & productCode & ", " & patchCode & ", " & location & vbCrLf
			output = output & location & vbCrLf
		Next
	Next
	output = output & "###end###"
	FindRegisteredPatches = output
End Function

' Debug mode
Function debug()
	Dim output : output = "###begin###" & vbCrLf
	Dim file1 : file1 = "E:\tmp\Crescent.mp3"
	Dim file2 : file2 = "E:\tmp\Lippincott.mp3"
	output = output & file1 & vbCrLf & file2 & vbCrLf
	output = output & "###end###"
	debug = output
End Function

wscript.echo FindRegisteredPatches
'wscript.echo debug
