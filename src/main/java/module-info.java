module se.uu.ub.cora.diva {
	requires se.uu.ub.cora.spider;
	requires se.uu.ub.cora.data;
	requires se.uu.ub.cora.sqldatabase;
	requires se.uu.ub.cora.httphandler;
	requires se.uu.ub.cora.logger;
	requires se.uu.ub.cora.storage;
	// requires se.uu.ub.cora.diva.mixedstorage;
	requires se.uu.ub.cora.fedora;
	requires se.uu.ub.cora.converter;
	requires se.uu.ub.cora.xmlutils;

	provides se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionalityFactory
			with se.uu.ub.cora.diva.extended.DivaExtendedFunctionalityFactory;

}