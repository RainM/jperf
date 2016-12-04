package ru.ms.actors;

import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.model.MetaClass;
import org.adoptopenjdk.jitwatch.model.MetaPackage;
import org.adoptopenjdk.jitwatch.model.PackageManager;

import java.util.function.Consumer;

/**
 * Created by sergey on 06.11.16.
 */
public class Utils {
//    private static void listAllClasses(PackageManager pm, Consumer<MetaClass> func) {
//        for (MetaPackage mp : pm.getRootPackages()) {
//            listAllClasses(mp, func);
//        }
//    }
//
//
//
//    /*
//        private static void listAllMethods(
//                PackageManager pm,
//                Predicate<MetaPackage> packagePredicate,
//                Predicate<MetaClass> classPredicate,
//                Predicate<IMetaMember> memberPredicate,
//                Consumer<IMetaMember> func
//                )
//        {
//            for (MetaPackage mp : pm.getRootPackages()) {
//                listAllMethods(
//                        mp,
//                        packagePredicate,
//                        classPredicate,
//                        memberPredicate,
//                        func);
//            }
//        }*/
///*
//    public static void listAllMethods(
//            MetaPackage mp,
//            Predicate<MetaPackage> packagePredicate,
//            Predicate<MetaClass> classPredicate,
//            Predicate<IMetaMember> memberPredicate,
//            Consumer<IMetaMember> func
//    )
//    {
//        if (packagePredicate.test(mp)) {
//            for (MetaPackage cmp : mp.getChildPackages()) {
//                listAllMethods(
//                        cmp,
//                        packagePredicate,
//                        classPredicate,
//                        memberPredicate,
//                        func
//                );
//            }
//            for (MetaClass mc : mp.getPackageClasses()) {
//
//                    for (IMetaMember mm : mc.getMetaMembers()) {
//                        if (classPredicate.test(mc)) {
//                            if (memberPredicate.test(mm)) {
//                                func.accept(mm);
//                            }
//                    }
//                }
//            }
//        }
//    }
//*/
//    public static boolean checkPackageBelongs(String opts, MetaPackage mp) {
//        if (opts == null || opts.isEmpty()) return true;
//        return mp.getName().matches(opts);
//    }
//
//    public static boolean checkClassBelongs(String opts, MetaClass mc) {
//        if (opts == null || opts.isEmpty()) return true;
//        return mc.getName().matches(opts);
//    }
//
//    public static boolean checkMethodBelongs(String opts, IMetaMember mm) {
//        if (opts == null || opts.isEmpty()) return true;
//        return mm.getFullyQualifiedMemberName().matches(opts);
//    }
}
