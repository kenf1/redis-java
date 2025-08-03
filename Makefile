.PHONY: gh cc clean

gh: #Push github repo master branch
	git push -u github master

cc: #Push cc test server
	git push origin

clean:
	rm -rf target