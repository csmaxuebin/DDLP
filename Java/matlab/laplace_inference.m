function [pr_post] = laplace_inference(p_prior, z, DeltaX, eps, T,MAX)

     Delta=T*DeltaX';
     [d,N]=size(T);
     if(size(DeltaX,1)~=1 )
         pr=sum(DeltaX);

     else
         pr=DeltaX;
     end

if(MAX==0)
   pr_post=p_prior;
   return;
end

     col=1;
     for i=1:N
         if pr(1,i)==1;

             %m=z(2,1)*grid_num+z(1,1);
            % n=Delta(2,col)*grid_num+Delta(1,col);
            % pr(1,i)=(eps/(2*MAX)) * exp((-abs(m-n)*eps)/MAX);'

             pr(1,i)=(eps/(2*MAX)) * exp((-norm(z-Delta(:,col),1)*eps)/MAX);
             col=col+1;
         end
     end

     pr_post=pr.*p_prior;
     pr_post=pr_post/sum(pr_post);%Bayesian inference;

     if(sum(pr_post)==0 || sum(pr_post==NaN)>0)
         pr_post=p_prior;
     end
end