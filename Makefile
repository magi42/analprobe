all: copy

copy:
	tar c -C src --exclude='*svn*' com|tar x -C build/classes/
#	cd build ; ant refresh-eclipse

publish: copy
	cd build ; ant package-war
	scp build/result/analprobe.war magivp:.
	ssh magivp "mv analprobe.war webapps"
