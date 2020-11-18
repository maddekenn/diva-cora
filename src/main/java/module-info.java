module se.uu.ub.cora.diva {
	requires se.uu.ub.cora.spider;
	requires se.uu.ub.cora.data;
	requires se.uu.ub.cora.sqldatabase;

	provides se.uu.ub.cora.spider.extendedfunctionality.ExtendedFunctionalityFactory
			with se.uu.ub.cora.diva.DivaExtendedFunctionalityFactory;

}