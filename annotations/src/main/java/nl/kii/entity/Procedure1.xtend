package nl.kii.entity

/** Alternative for org.eclipse.xtext.xbase.lib.Procedures.Procedure1 to be compatible with byte-code instrumentation in constructors */
interface Procedure1<P1> {
	def void apply(P1 parameter)
}